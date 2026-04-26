# API Gateway

Servicio de entrada HTTP del ecosistema Eco Store. Recibe requests REST y los enruta a los microservicios internos usando NATS (request/reply), ademas de exponer Swagger y el endpoint webhook para PayPal.

## Que hace

- Centraliza el acceso de clientes (frontend, mobile, integraciones) en una sola API.
- Traduce endpoints HTTP a mensajes NATS hacia:
  - `product-service`
  - `cart-service`
  - `checkout-service`
- Unifica manejo de errores HTTP a partir de respuestas de microservicios.
- Publica documentacion OpenAPI/Swagger.
- Reenvia webhook de PayPal a `checkout-service`.

## Dependencias

- Java 25
- Maven 3.9+
- NATS disponible (default: `nats://localhost:4222`)
- `checkout-service` accesible para pass-through de webhook

## Configuracion

Archivo: `src/main/resources/application.properties`

- `server.port=8080`
- `nats.url=nats://localhost:4222`
- `nats.request-timeout-ms=15000`
- `checkout.webhook-base-url=http://checkout-service:8083`
- `springdoc.api-docs.path=/v1/api-docs`
- `springdoc.swagger-ui.path=/swagger-ui.html`

### Documentacion

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v1/api-docs`

## Lista de endpoints

### Productos

- `POST /api/v1/products` - Crear producto
- `GET /api/v1/products/{id}` - Obtener producto por ID
- `GET /api/v1/products` - Listar productos
- `PUT /api/v1/products/{id}` - Actualizar producto
- `DELETE /api/v1/products/{id}` - Eliminar producto

### Carrito

- `POST /api/v1/carts/{cartId}/items` - Agregar producto al carrito
- `DELETE /api/v1/carts/{cartId}/items/{productId}` - Eliminar item del carrito
- `GET /api/v1/carts/{cartId}` - Obtener carrito
- `POST /api/v1/carts/{cartId}/checkout/init` - Iniciar checkout (crea orden de pago)

### Checkout / Webhook

- `POST /api/v1/checkout/paypal/webhook` - Pass-through del webhook de PayPal hacia `checkout-service`
