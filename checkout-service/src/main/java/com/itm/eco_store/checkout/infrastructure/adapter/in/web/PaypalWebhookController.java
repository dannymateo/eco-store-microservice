package com.itm.eco_store.checkout.infrastructure.adapter.in.web;

import com.itm.eco_store.checkout.application.port.in.ConfirmCheckoutUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout/paypal")
public class PaypalWebhookController {

    private final ConfirmCheckoutUseCase confirmCheckoutUseCase;

    public PaypalWebhookController(ConfirmCheckoutUseCase confirmCheckoutUseCase) {
        this.confirmCheckoutUseCase = confirmCheckoutUseCase;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody JsonNode payload) {
        String eventType = payload.path("event_type").asText("");
        String orderReference = extractOrderReference(payload);
        if (orderReference == null || orderReference.isBlank()) {
            return ResponseEntity.ok(Map.of("received", true, "ignored", true, "reason", "orderReference no encontrado"));
        }

        if ("CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(eventType)) {
            try {
                var result = confirmCheckoutUseCase.confirm("PAYPAL", orderReference);
                return ResponseEntity.ok(Map.of(
                        "received", true,
                        "processed", true,
                        "orderReference", orderReference,
                        "status", result.getStatus().name()
                ));
            } catch (Exception ex) {
                return ResponseEntity.status(mapErrorStatus(ex.getMessage()))
                        .body(Map.of("received", true, "processed", false, "error", ex.getMessage()));
            }
        }

        return ResponseEntity.ok(Map.of("received", true, "ignored", true, "eventType", eventType));
    }

    private String extractOrderReference(JsonNode payload) {
        JsonNode resource = payload.path("resource");
        String directId = resource.path("id").asText("");
        if (!directId.isBlank() && "CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(payload.path("event_type").asText(""))) {
            return directId;
        }
        String relatedOrderId = resource.path("supplementary_data")
                .path("related_ids")
                .path("order_id")
                .asText("");
        return relatedOrderId.isBlank() ? null : relatedOrderId;
    }

    private HttpStatus mapErrorStatus(String error) {
        if (error == null || error.isBlank()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String lowered = error.toLowerCase();
        if (lowered.contains("no encontrado") || lowered.contains("no existe")) {
            return HttpStatus.NOT_FOUND;
        }
        if (lowered.contains("obligatoria")
                || lowered.contains("obligatorio")
                || lowered.contains("invalido")
                || lowered.contains("inválido")) {
            return HttpStatus.BAD_REQUEST;
        }
        if (lowered.contains("order_not_approved") || lowered.contains("not approved")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
