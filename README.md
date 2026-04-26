# Eco Store Microservice Backend

Manual de instalacion paso a paso para levantar el entorno completo desde la raiz del monorepo.


## 1) Abrir el proyecto

Clona el repositorio y entra a la carpeta raiz:

```bash
git clone <URL_DEL_REPO>
cd eco-store-microservice-backend
```

```bash
cd eco-store-microservice-backend
```

## 2) Crear archivo de variables `.env`

En la raiz del monorepo, crea `.env` con este contenido:

```env
PAYPAL_CLIENT_ID=tu_paypal_client_id
PAYPAL_CLIENT_SECRET=tu_paypal_client_secret
PAYPAL_BASE_URL=https://api-m.sandbox.paypal.com
PAYPAL_RETURN_URL=http://localhost:3000/checkout/success
PAYPAL_CANCEL_URL=http://localhost:3000/checkout/cancel
```

## 3) Levantar todo el entorno

Desde la raiz del monorepo ejecuta:

```bash
docker compose up -d --build
```

Esto levanta:

- `nats` (4222)
- `postgres` product (5432)
- `checkout-postgres` (5433 externo)
- `redis` (6379)
- `product-service` (8081)
- `cart-service` (8082)
- `checkout-service` (8083)
- `api-gateway` (8080)

## 4) Verificar que todo este arriba

Ver estado de contenedores:

```bash
docker compose ps
```

Abrir Swagger:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
