# Subscription Capacity Allocator

This is a small Spring Boot REST API I built that solves a capital allocation problem: given a list of subscription requests (each asking for some amount of capital and offering some fee revenue) and a max capacity, it picks the subset of requests that gives the most total fee revenue without going over capacity.

## How to build and run it

Java,Docker and Maven is needed.

**1. Clone the repo**
```bash
git clone https://github.com/zidhartha/SubscriptionCapacityAllocator.git
cd subscription-capacity-allocator
```

**2. Start the database**
```bash
docker compose up -d
```
This spins up a Postgres container using the settings in `docker-compose.yml`.

**3. Run the app**
```bash
mvn spring-boot:run
```

The app will start on `http://localhost:8080`.

**4. Run the tests**
```bash
mvn test
```
the database container needs to be running for the integration tests to pass.

**5. Build and run as a JAR**
```bash
mvn clean package
java -jar target/SubscriptionCapacityAllocator-0.0.1-SNAPSHOT.jar
```

## Database setup

I used PostgreSQL running in Docker (see `docker-compose.yml`). It's exposed on port **5433** on the host (not the default 5432 since something was blocking it on my leptop), so keep that in mind.

Flyway manages the schema — the migration script lives at `src/main/resources/db/migration/V1__create_allocation_tables.sql`.

There are two tables:

- **`allocation_decision`** — one row per time someone calls `POST /optimize`. Columns: `decision_id` (primary key), `request_id` (UUID, this is what you use to look the decision back up later), `max_capacity`, `total_requested_amount`, `total_fee_revenue`, and `created_at`.
- **`accepted_subscriptions`** — one row per accepted request within a decision. Columns: `id` (primary key), `investor_name`, `requested_amount`, `fee_revenue`, and `decision_id` (foreign key back to `allocation_decision`). This is a one-to-many relationship — one decision can have many accepted subscriptions.

### Index choices

- **Unique index on `allocation_decision.request_id`** — this is the field `GET /{requestId}` looks up by,it needs to be unique anyway since its a primary key.But since it is indexed, full table scan does not happen and the lookup is much faster because it creates a btree which has a faster lookup.
- **Index on `allocation_decision.created_at` (descending)** — `GET /subscriptions` returns results ordered newest first, the same logic applies to this index, since this column is used for sorting, the index helps to make the process faster.
- **Index on `accepted_subscriptions.decision_id`** — this is the foreign key back to the parent decision, and this index will help the join to be faster.

## API examples

### 1. `POST /api/v1/subscriptions/optimize`

Request:
```bash
curl -X POST http://localhost:8080/api/v1/subscriptions/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "maxCapacity": 15,
    "availableSubscriptions": [
      { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 120 },
      { "investorName": "Investor B", "requestedAmount": 10, "feeRevenue": 200 },
      { "investorName": "Investor C", "requestedAmount": 3, "feeRevenue": 80 },
      { "investorName": "Investor D", "requestedAmount": 8, "feeRevenue": 160 }
    ]
  }'
```

Response (`201 Created`):
```json
{
  "requestId": "f3055762-0e01-4e9a-b57a-bc8c2590b8a2",
  "acceptedSubscriptions": [
    { "investorName": "Investor B", "requestedAmount": 10, "feeRevenue": 200 },
    { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 120 }
  ],
  "totalRequestedAmount": 15,
  "totalFeeRevenue": 320,
  "createdAt": "2026-08-26T16:30:23.049054"
}
```

A + B was chosen over C + D or any other combo because it gives the highest total fee (320) while staying within capacity 15.

### 2. `GET /api/v1/subscriptions/{requestId}`

Request (using the `requestId` from above):
```bash
curl http://localhost:8080/api/v1/subscriptions/f3055762-0e01-4e9a-b57a-bc8c2590b8a2
```

Response (`200 OK`):
```json
{
  "requestId": "f3055762-0e01-4e9a-b57a-bc8c2590b8a2",
  "acceptedSubscriptions": [
    { "investorName": "Investor B", "requestedAmount": 10, "feeRevenue": 200 },
    { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 120 }
  ],
  "totalRequestedAmount": 15,
  "totalFeeRevenue": 320,
  "createdAt": "2026-08-26T16:30:23.049054"
}
```

If you use a `requestId` that doesn't exist, you get a `404 Not Found` instead.

### 3. `GET /api/v1/subscriptions`

Request:
```bash
curl http://localhost:8080/api/v1/subscriptions
```

Response (`200 OK`), paginated list of every decision made so far, most recent first:
```json
{
  "content": [
    {
      "requestId": "f3055762-0e01-4e9a-b57a-bc8c2590b8a2",
      "acceptedSubscriptions": [
        { "investorName": "Investor B", "requestedAmount": 10, "feeRevenue": 200 },
        { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 120 }
      ],
      "totalRequestedAmount": 15,
      "totalFeeRevenue": 320,
      "createdAt": "2026-08-26T16:30:23.049054"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

### Error case example

If you send invalid input (like a negative capacity), you get a `400 Bad Request` with a message explaining what's wrong:
```bash
curl -X POST http://localhost:8080/api/v1/subscriptions/optimize \
  -H "Content-Type: application/json" \
  -d '{ "maxCapacity": -5, "availableSubscriptions": [] }'
```
```json
{ "error": "Maximum capacity must be non negative" }
```

## A note on the algorithm

I solved this with dynamic programming rather than a greedy approach (like "just take the highest-fee requests first") because greedy doesn't actually give the optimal answer for this kind of problem. For example: if request A costs 6 and earns 60, request B costs 10 and earns 100, and request C costs 4 and earns 60, and capacity is 10 — greedy would just take B (fee 100), but the actual best answer is A + C together (fee 120). The DP solution correctly finds combinations like this instead of just grabbing the single best option.