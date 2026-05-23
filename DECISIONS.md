# Design Decisions

## 1. Manual Admin Discount Code Generation

**Context:**
The system needs to issue a discount coupon when every nth order is placed. The question was whether the code should be generated automatically at checkout or manually by an admin.

**Options Considered:**
- **Auto-generate at checkout:** System creates and attaches the code immediately when the nth order is placed
- **Manual admin action:** Checkout signals that the nth order was reached; admin explicitly calls an endpoint to generate the code

**Final Choice:** Manual admin action via `POST /api/admin/generate-discount`

**Reasoning:**
The spec explicitly calls for an admin endpoint. Beyond that, manual generation gives the business control over when to release a code — they may want to review, delay, or decide not to issue one for a particular nth order. Auto-generation removes that flexibility.

---

## 2. nthOrderReached Flag as a Hint, Not a Trigger

**Context:**
With manual admin generation chosen, the admin needs to know when the nth order has been placed so they know when to call generate-discount.

**Options Considered:**
- Admin polls the analytics endpoint and manually checks order count
- Checkout response includes a `nthOrderReached: true` flag as a signal
- A separate notification or webhook system

**Final Choice:** `nthOrderReached: true` in the checkout response body

**Reasoning:**
Simple and stateless. The flag is just a hint — the admin sees it in the response and knows to act. No polling needed, no infrastructure for webhooks. Importantly, this flag is never trusted by the server for code generation (see Decision 3).

---

## 3. Server-Side Validation in Generate-Discount (Independent of Client Hint)

**Context:**
Since the checkout response carries `nthOrderReached: true`, there is a risk: if any upstream service passes this flag incorrectly (bug, bad actor, misconfiguration), the admin could generate a code that was never earned.

**Options Considered:**
- Trust the hint — if admin calls generate-discount, assume conditions are met
- Re-validate server-side by checking `orderCount % n == 0` at the moment of the call
- Use a `pendingCodeGeneration` flag set by the server itself at checkout time, and validate against that

**Final Choice:** Server-side `pendingCodeGeneration` flag set at checkout, validated independently in generate-discount

**Reasoning:**
The `nthOrderReached` field in the response is purely informational. The `pendingCodeGeneration` flag in `GeoState` is the authoritative source of truth — it is set by the server only when a real nth order occurs, and cleared only when a code is generated. This means the admin endpoint never trusts the client. Even if the hint is wrong, the system protects itself.

---

## 4. Geo-Scoped nth Order Tracking with Admin-Configurable n and x%

**Context:**
The system needs to define what "every nth order" means and track progress toward it. The question was whether this is global across all users or scoped in some way, and who controls the values.

**Options Considered:**
- Global order count, hardcoded n and x%
- Per-user order count (every nth order a specific user places)
- Per-geo order count, with n and x% configurable per geo via admin API

**Final Choice:** Per-geo tracking with admin-configurable n and x% via `POST /api/admin/config`

**Reasoning:**
Geo-scoped tracking reflects real-world ecommerce — a promotion in the US should not interfere with one in India. Making n and x% admin-configurable (not hardcoded or env vars) allows the business to adjust promotions per region without redeploying. When n is updated mid-flight, the new value applies immediately to the current count with no reset — consistent with how most business rule changes work.

---

## 5. Discount Code is Single-Use and User-Specific

**Context:**
Once a discount code is generated, decisions needed to be made around who can use it and how many times.

**Options Considered:**
- Anyone with the code can use it (shareable)
- Only the user who triggered the nth order can use it, single-use
- Only the user who triggered the nth order can use it, multi-use

**Final Choice:** Single-use, tied to the specific user who placed the nth order

**Reasoning:**
The discount is a reward for the user who happened to place the nth order — not a general promotion. Allowing anyone to use it would dilute the intent and create a code-sharing abuse vector. Single-use prevents the same user from applying it repeatedly. The `userId` and `used` fields on `DiscountCode` enforce both constraints at validation time.

---

## 6. One Active Discount Code Per Geo at a Time

**Context:**
If n=3 and the admin does not generate a code after order 3, then order 6 is reached — should a second code be allowed to pile up?

**Options Considered:**
- Allow multiple active (unused) codes to accumulate per geo
- Allow only one active code per geo at any time

**Final Choice:** Only one active (unused) code per geo at a time

**Reasoning:**
Multiple active codes would create ambiguity about which code is valid and could lead to unintended discounts if old codes surface. The admin must generate and the user must redeem before a new code can be issued. This keeps the discount lifecycle explicit and auditable.

---

## 7. Repository Pattern for In-Memory Storage

**Context:**
The spec requires in-memory storage with no database. However, the code should be structured so that storage can be swapped later without touching business logic.

**Options Considered:**
- Use `HashMap` directly inside service classes
- Create repository interfaces with in-memory implementations behind them

**Final Choice:** Repository interfaces (`CartRepository`, `OrderRepository`, `DiscountRepository`, `GeoRepository`) with `InMemory*` implementations

**Reasoning:**
Services depend only on interfaces — they have no knowledge of how data is stored. Swapping to a database later means writing a new implementation class and changing a Spring bean registration, nothing else. This also made unit testing straightforward: tests instantiate the `InMemory` implementations directly without needing Spring context or mocking.

---

## 8. Synchronous Checkout-to-Admin Notification

**Context:**
After a checkout signals `nthOrderReached: true`, the admin needs to act. The question was whether this notification should be asynchronous (event-driven) or synchronous (the hint in the response is enough).

**Options Considered:**
- Async: checkout publishes an event to a message queue (Kafka/RabbitMQ); admin service subscribes
- Synchronous: `nthOrderReached` flag in the checkout response, admin acts manually

**Final Choice:** Synchronous — flag in checkout response, admin calls generate-discount manually

**Reasoning:**
The spec calls for manual admin action and in-memory storage. Introducing a message queue would add significant infrastructure complexity with no corresponding benefit for this scope. The synchronous approach is simpler, easier to test, and sufficient. If the system scales to high traffic and real-time notification is needed, the repository pattern (Decision 7) means the storage swap is already handled — adding async eventing would be the only new concern.

---

## 9. Design Patterns Applied

**Context:**
The assignment evaluates thinking process and code quality. Applying standard design patterns where they fit naturally demonstrates intentional design.

**Patterns Used and Why:**

| Pattern | Where | Reason |
|---|---|---|
| **Repository** | Storage layer | Decouples business logic from storage (see Decision 7) |
| **Strategy** | Discount calculation | `DiscountStrategy` interface allows new discount types (flat, tiered) without changing service code |
| **Factory** | Code generation | `DiscountCodeFactory` centralises code creation — format, uniqueness, and timestamp logic in one place |
| **Builder** | `Order` construction | Orders have many fields; builder avoids telescoping constructors and makes construction readable |
| **Singleton** | Spring `@Service` beans | Spring manages all services as singletons by default — the in-memory store is naturally shared |
