package com.itm.api_gateway.web.cartgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "Payload para procesar checkout",
        example = "{\"paymentMethod\":\"PAYPAL\",\"payerEmail\":\"buyer@eco-store.com\"}"
)
public record CheckoutRequest(
        @Schema(description = "Metodo de pago", example = "PAYPAL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El metodo de pago es obligatorio")
        String paymentMethod,
        @Schema(description = "Correo del pagador", example = "buyer@eco-store.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "El correo del pagador no es valido")
        @NotBlank(message = "El correo del pagador es obligatorio")
        String payerEmail
) {
}
