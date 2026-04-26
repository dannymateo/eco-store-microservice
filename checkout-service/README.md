# Checkout Service

Microservicio encargado del proceso de pago y confirmacion de compra. Implementado con Spring Boot y arquitectura hexagonal.

## Resumen para usuarios no tecnicos

Este servicio transforma un carrito en una compra pagada. Verifica disponibilidad de stock, crea la orden de pago en PayPal y confirma la compra cuando PayPal aprueba el pago.

## Que hace tecnicamente

- Guarda checkouts e items facturados en PostgreSQL.
- Lee el carrito desde `cart-service` por NATS.
- Valida stock de productos al iniciar checkout.
- Crea y captura orden de pago (PayPal).
- Descuenta stock en `product-service` cuando el pago se aprueba.
- Limpia el carrito al confirmar pago.
- Recibe webhook de PayPal:
  - `POST /api/v1/checkout/paypal/webhook`
- Subjects NATS principales:
  - `v1.checkout.init`
  - `v1.checkout.confirm`
  - `v1.cart.get`
  - `v1.cart.clear`
  - `v1.catalog.product.get`
  - `v1.catalog.product.update`

## Dependencias

- Java 25
- Maven
- PostgreSQL
- NATS
- Credenciales de PayPal (sandbox o live)

## Variables de configuracion

Archivo: `src/main/resources/application.properties`

- `SERVER_PORT` (default: `8083`)
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/checkout_db`)
- `SPRING_DATASOURCE_USERNAME` (default: `checkout_user`)
- `SPRING_DATASOURCE_PASSWORD` (default: `checkout_pass`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (default: `update`)
- `NATS_URL` (default: `nats://localhost:4222`)
- `PAYPAL_CLIENT_ID`
- `PAYPAL_CLIENT_SECRET`
- `PAYPAL_BASE_URL` (default: sandbox)
- `PAYPAL_RETURN_URL`
- `PAYPAL_CANCEL_URL`

## Configuracion de PayPal

En la raiz del proyecto usa `.env` con:

```env
PAYPAL_CLIENT_ID=tu_client_id
PAYPAL_CLIENT_SECRET=tu_client_secret
PAYPAL_BASE_URL=https://api-m.sandbox.paypal.com
PAYPAL_RETURN_URL=https://tu-frontend.example.com/checkout/success
PAYPAL_CANCEL_URL=https://tu-frontend.example.com/checkout/cancel
```

## Prueba rapida

- Iniciar checkout desde `api-gateway` (Swagger).
- Abrir `approvalUrl` devuelto por PayPal.
- Confirmacion automatica por webhook en `/api/v1/checkout/paypal/webhook`.

## Rol dentro del sistema

`checkout-service` es el orquestador de compra: valida, cobra, confirma, descuenta stock y cierra el carrito.
