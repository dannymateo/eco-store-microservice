# Cart Service

Microservicio encargado del carrito de compras. Implementado con Spring Boot y arquitectura hexagonal.

## Resumen para usuarios no tecnicos

Este servicio representa el "carrito" del cliente. Permite agregar y quitar productos, consultar el estado del carrito y marcarlo para checkout.

## Que hace tecnicamente

- Persiste carritos en Redis.
- Consulta producto por NATS para validar datos al agregar al carrito.
- Expone comandos por NATS:
  - `v1.cart.add-product`
  - `v1.cart.remove-product`
  - `v1.cart.get`
  - `v1.cart.checkout`
  - `v1.cart.clear`
- Publica evento de checkout completado:
  - `v1.cart.checkout.completed`

## Dependencias

- Java 25
- Maven
- Redis
- NATS

## Variables de configuracion

Archivo: `src/main/resources/application.properties`

- `SERVER_PORT` (default: `8082`)
- `SPRING_REDIS_HOST` (default: `localhost`)
- `SPRING_REDIS_PORT` (default: `6379`)
- `NATS_URL` (default: `nats://localhost:4222`)
- Subjects NATS configurables para cart y product.

## Prueba rapida

Puedes probar operaciones de carrito desde el `api-gateway` en `http://localhost:8080/swagger-ui.html`.

## Rol dentro del sistema

`cart-service` mantiene el estado temporal de compra del cliente. No procesa pagos; su responsabilidad termina al preparar y exponer el carrito para `checkout-service`.
