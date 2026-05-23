# Ecommerce Store API

A backend API for an ecommerce platform with shopping cart, checkout, and a geo-scoped discount reward system.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Maven
- In-memory storage (no database)
- JUnit 5

## Setup

```bash
git clone https://github.com/pankhil7/ecommerce-assignment
cd ecommerce-assignment
mvn spring-boot:run
```

Server starts on `http://localhost:8081`

## Run Tests

```bash
mvn test
```

## API Endpoints

| Method | Endpoint | Who |
|---|---|---|
| POST | `/api/cart/add` | Customer |
| POST | `/api/checkout` | Customer |
| POST | `/api/admin/config` | Admin |
| POST | `/api/admin/generate-discount` | Admin |
| GET | `/api/admin/analytics` | Admin |

---

## Core Flow with curl

### 1. Configure Geo (Admin)

Set `n` (every nth order) and discount percentage per geo. New values apply immediately.

```bash
curl -X POST http://localhost:8081/api/admin/config \
  -H "Content-Type: application/json" \
  -d '{"geoId":"US","nthOrder":3,"discountPercentage":10}'
```

```json
{
  "geoId": "US",
  "nthOrder": 3,
  "discountPercentage": 10.0
}
```

---

### 2. Add Items to Cart

```bash
curl -X POST http://localhost:8081/api/cart/add \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","geoId":"US","productId":"p1","name":"Headphones","price":100.0,"quantity":1}'
```

```json
{
  "userId": "user1",
  "geoId": "US",
  "items": [
    { "productId": "p1", "name": "Headphones", "price": 100.0, "quantity": 1 }
  ]
}
```

---

### 3. Checkout (no discount)

```bash
curl -X POST http://localhost:8081/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","geoId":"US"}'
```

```json
{
  "orderId": "0bfe60c4-7e49-49f0-8fc8-75e5bd9c1667",
  "subtotal": 100.0,
  "discountAmount": 0.0,
  "total": 100.0,
  "nthOrderReached": false
}
```

---

### 4. nth Order Reached

When the 3rd order is placed, `nthOrderReached: true` signals the admin to generate a code.

```json
{
  "orderId": "bc80be62-c6f9-437f-8080-766802dddf86",
  "subtotal": 200.0,
  "discountAmount": 0.0,
  "total": 200.0,
  "nthOrderReached": true
}
```

---

### 5. Admin Generates Discount Code

The server validates independently — does not trust the client hint.

```bash
curl -X POST http://localhost:8081/api/admin/generate-discount \
  -H "Content-Type: application/json" \
  -d '{"geoId":"US"}'
```

```json
{
  "code": "4B84694AFF84",
  "geoId": "US",
  "userId": "user3",
  "percentage": 10.0,
  "used": false,
  "createdAt": "2026-05-23T18:38:15.522771"
}
```

---

### 6. Checkout with Discount Code

Only the user who placed the nth order can redeem the code.

```bash
curl -X POST http://localhost:8081/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId":"user3","geoId":"US","discountCode":"4B84694AFF84"}'
```

```json
{
  "orderId": "3a3e5d81-ed2e-467d-9a8b-ae3825b3f09d",
  "subtotal": 150.0,
  "discountAmount": 15.0,
  "total": 135.0,
  "nthOrderReached": false
}
```

---

### 7. Analytics (Admin)

```bash
curl "http://localhost:8081/api/admin/analytics?geoId=US"
```

```json
{
  "totalOrders": 4,
  "totalRevenue": 515.0,
  "totalDiscountAmount": 15.0,
  "discountCodes": [
    {
      "code": "4B84694AFF84",
      "geoId": "US",
      "userId": "user3",
      "percentage": 10.0,
      "used": true,
      "createdAt": "2026-05-23T18:38:15.522771"
    }
  ]
}
```

---

## Error Cases

### Empty cart checkout
```json
{ "error": "Cart not found for user: ghost" }
```

### Invalid or already-used discount code
```json
{ "error": "Invalid or expired discount code" }
```

### Admin generates code when conditions not met
```json
{ "error": "Conditions not met: nth order threshold not reached for geo US" }
```

### Wrong user tries another user's code
```json
{ "error": "Invalid or expired discount code" }
```

---

## Logs (Key Events)

```
INFO  AdminService     : Geo configured [geoId=US, nthOrder=3, discountPercentage=10.0%]
INFO  CartService      : Adding item [productId=p1] to cart [userId=user1, geoId=US]
INFO  OrderService     : Checkout initiated [userId=user1, geoId=US, hasDiscountCode=false]
INFO  OrderService     : Order created [orderId=..., userId=user1, total=100.0]
INFO  CartService      : Clearing cart for user [user1]
INFO  OrderService     : nth order reached [geo=US, orderCount=3, eligibleUserId=user3]
INFO  AdminService     : Admin requested discount code generation [geoId=US]
INFO  DiscountService  : Discount code generated [code=4B84694AFF84, geo=US, userId=user3, percentage=10.0%]
WARN  DiscountService  : Discount code validation failed [code=4B84694AFF84, userId=user1, geoId=US]
WARN  GlobalExHandler  : Bad request: Invalid or expired discount code
INFO  DiscountService  : Marking discount code as used [code=4B84694AFF84]
```

---

## Design

See [DECISIONS.md](./DECISIONS.md) for detailed design decisions including discount flow, geo scoping, server-side validation, and patterns used.
