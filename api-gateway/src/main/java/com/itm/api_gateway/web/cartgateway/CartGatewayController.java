package com.itm.api_gateway.web.cartgateway;

import com.itm.api_gateway.config.NatsGatewayProperties;
import com.itm.api_gateway.messaging.NatsRequestClient;
import com.itm.api_gateway.messaging.NatsResponse;
import com.itm.api_gateway.web.cartgateway.dto.AddProductToCartRequest;
import com.itm.api_gateway.web.cartgateway.dto.CheckoutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
public class CartGatewayController {

    private final NatsRequestClient natsRequestClient;
    private final NatsGatewayProperties properties;

    public CartGatewayController(NatsRequestClient natsRequestClient, NatsGatewayProperties properties) {
        this.natsRequestClient = natsRequestClient;
        this.properties = properties;
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Agregar producto al carrito", description = "Valida existencia del producto y agrega cantidades al carrito.")
    public ResponseEntity<?> addProduct(
            @Parameter(example = "cart-123") @PathVariable String cartId,
            @Valid @RequestBody AddProductToCartRequest body
    ) {
        NatsResponse response = natsRequestClient.request(
                properties.subject().cart().addProduct(),
                Map.of("cartId", cartId, "productId", body.productId(), "quantity", body.quantity())
        );
        return toHttpResponse(response, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Eliminar item del carrito", description = "Elimina un producto específico del carrito.")
    public ResponseEntity<?> removeProduct(
            @Parameter(example = "cart-123") @PathVariable String cartId,
            @Parameter(example = "1") @PathVariable Long productId
    ) {
        NatsResponse response = natsRequestClient.request(
                properties.subject().cart().removeProduct(),
                Map.of("cartId", cartId, "productId", productId)
        );
        return toHttpResponse(response, HttpStatus.OK);
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Obtener carrito", description = "Retorna el estado actual del carrito.")
    public ResponseEntity<?> getCart(@Parameter(example = "cart-123") @PathVariable String cartId) {
        NatsResponse response = natsRequestClient.request(properties.subject().cart().get(), Map.of("cartId", cartId));
        return toHttpResponse(response, HttpStatus.OK);
    }

    @PostMapping("/{cartId}/checkout/init")
    @Operation(summary = "Iniciar checkout PayPal", description = "Crea la orden de PayPal y devuelve URL de aprobacion.")
    public ResponseEntity<?> initCheckout(
            @Parameter(example = "cart-123") @PathVariable String cartId,
            @Valid @RequestBody CheckoutRequest body
    ) {
        NatsResponse response = natsRequestClient.request(
                properties.subject().checkout().init(),
                Map.of(
                        "cartId", cartId,
                        "paymentMethod", body.paymentMethod(),
                        "payerEmail", body.payerEmail()
                )
        );
        return toHttpResponse(response, HttpStatus.OK);
    }

    private ResponseEntity<?> toHttpResponse(NatsResponse response, HttpStatus successStatus) {
        if (response.success()) {
            return ResponseEntity.status(successStatus).body(response.data());
        }
        HttpStatus status = mapErrorStatus(response.error());
        return ResponseEntity.status(status).body(Map.of("error", response.error()));
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
        if (lowered.contains("ya fue cerrado") || lowered.contains("sin items") || lowered.contains("stock insuficiente")) {
            return HttpStatus.CONFLICT;
        }
        if (lowered.contains("order_not_approved") || lowered.contains("not approved")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
