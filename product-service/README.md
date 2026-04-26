# Product Service

Microservicio encargado del catalogo de productos. Implementado con Spring Boot y arquitectura hexagonal.

## Resumen para usuarios no tecnicos

Este servicio es el "inventario" de la tienda. Guarda los productos (nombre, descripcion, precio y stock) y permite crear, consultar, actualizar y eliminar productos.

## Que hace tecnicamente

- Administra productos en PostgreSQL.
- Expone operaciones por mensajeria NATS.
- Publica y responde en subjects de catalogo:
  - `v1.catalog.product.create`
  - `v1.catalog.product.get`
  - `v1.catalog.product.list`
  - `v1.catalog.product.update`
  - `v1.catalog.product.delete`

## Dependencias

- Java 25
- Maven
- PostgreSQL
- NATS

## Variables de configuracion

Archivo: `src/main/resources/application.properties`

- `SERVER_PORT` (default: `8081`)
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/product_db`)
- `SPRING_DATASOURCE_USERNAME` (default: `product_user`)
- `SPRING_DATASOURCE_PASSWORD` (default: `product_pass`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (default: `update`)
- `NATS_URL` (default: `nats://localhost:4222`)
- `APP_SEED_PRODUCTS_ENABLED` (default: `true`)

## Prueba rapida

Puedes probar el flujo por medio del `api-gateway` en `http://localhost:8080/swagger-ui.html`.

## Rol dentro del sistema

`product-service` es la fuente de verdad del catalogo y del stock. Otros microservicios (como `cart-service` y `checkout-service`) consultan y actualizan productos a traves de NATS.
