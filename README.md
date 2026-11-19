# Order Management Service

A backend for a simple stock broker. Users place buy and sell orders against
live prices, and their cash balance updates as they trade.

Built with Spring Boot, Postgres and Redis. The point of the project is the
split between the two data stores: Postgres holds everything that must survive
a restart, and Redis holds data that is hot, changes constantly, or is cheap to
rebuild.

## Stack

- Java 17, Spring Boot 4.1
- Postgres 16 — orders, users, instruments
- Redis 7 — live prices and a read cache
- Flyway — schema migrations
- Docker Compose — both databases

## Running it

You need Java 17+, Maven and Docker Desktop.

```bash
docker compose up -d      # starts Postgres and Redis
./mvnw spring-boot:run    # starts the application
```

Flyway builds the schema on first start and inserts sample instruments and
users. Nothing else to set up.

Postgres is mapped to **port 5433** on the host, not the default 5432, so it
does not clash with a locally installed Postgres.

## Endpoints

| Method | Path | What it does |
| --- | --- | --- |
| GET | `/api/v1/instruments` | List all tradable instruments |
| GET | `/api/v1/instruments/{symbol}` | One instrument (cached in Redis) |
| GET | `/api/v1/instruments/{symbol}/price` | Current live price |
| POST | `/api/v1/orders` | Place a buy or sell order |
| GET | `/api/v1/orders?userId=1` | A user's orders, newest first |

`requests.http` in the project root has a runnable example of each, including
the failure cases.

### Placing an order

```json
POST /api/v1/orders

{
  "userId": 1,
  "symbol": "TCS",
  "side": "BUY",
  "quantity": 10
}
```

The price is not part of the request. The server fills at the current price,
which is what stops a client from buying at a price it chose itself.

## How the two data stores are used

### Postgres holds the truth

`users`, `instruments`, `orders`. Anything involving money or a record that
must not be lost.

Placing an order changes two things: the user's cash balance and a new order
row. Both happen inside one transaction, so either both land or neither does.
Without that, a failure between the two steps would deduct money with no order
to show for it.

### Redis holds what Postgres is bad at

Two different jobs, with deliberately different rules:

**`instrument:SYMBOL`** — a cached `InstrumentResponse` as JSON, 10 minute TTL.

This is a copy of data Postgres owns. The TTL exists because a copy can drift
from the original, so staleness is capped at ten minutes. Delete the key and
nothing is lost — the next read rebuilds it.

**`price:SYMBOL`** — the current price as a plain string, no TTL.

This is not a cache. Redis is the only place the live price exists. A scheduled
job nudges each price every second and writes it here. There is no expiry
because the key is not a copy of anything; expiring it would delete live data.

Writing this to Postgres instead would mean hundreds of thousands of row
updates a day for values nobody needs the history of. Losing it costs nothing —
the simulator rebuilds from the `last_price` column on the next tick.

Key names follow `<purpose>:<identifier>`. Redis has one flat namespace with no
schema, so the prefix is what tells you a value's shape.

### When Redis is down

Both read paths fall back to Postgres. Prices go stale, but orders still fill
and nothing is lost, because Redis holds nothing that cannot be rebuilt.

```bash
docker compose stop redis    # orders still work, at the database price
docker compose start redis   # the simulator picks straight back up
```

## Design decisions

**Flyway owns the schema, not Hibernate.** Migrations live in
`src/main/resources/db/migration` and run in order on every environment.
Letting Hibernate generate tables with `ddl-auto: update` is how production
columns get silently dropped.

**DTOs are separate from entities.** `InstrumentResponse` and `OrderResponse`
define what the API returns; the entities define what the tables hold. The two
change for different reasons. Adding a column does not change the API unless
someone deliberately adds it to the DTO.

**Validation in three layers.** The request DTO rejects malformed input before
any logic runs, the service enforces business rules like sufficient funds, and
the database holds `CHECK (quantity > 0)` as a last backstop.

**Errors go through one handler.** `GlobalExceptionHandler` decides every status
code, so services throw meaningful exceptions and never import HTTP classes.

**Plain foreign-key ids instead of `@ManyToOne`.** `Order` stores `userId` and
`instrumentId` as `Long` rather than mapped objects. This keeps query behaviour
explicit and avoids lazy-loading surprises, at the cost of fetching related
rows by hand.

**Money is `BigDecimal` and `NUMERIC(18,2)`, never a float.** Binary floating
point cannot represent 0.10 exactly, and those errors accumulate.

## Known gaps

Left out deliberately, not overlooked:

- **No authentication.** `userId` comes from the request body, so anyone can
  trade from any account. The fix is JWT: issue a token at login and read the
  user from the token instead.
- **Selling does not check holdings.** A user can sell shares they never bought
  and get paid. This needs a `holdings` table tracking quantity and average
  price per user per instrument.
- **N+1 query in the order listing.** Listing orders fetches each instrument's
  symbol separately — one query for the orders plus one per order. Fix is to
  fetch the instruments once into a map, or use a join query.
- **No cache invalidation on write.** There is no endpoint that updates an
  instrument yet. When there is, it must delete `instrument:SYMBOL` or the
  cache will serve stale data until the TTL expires.
- **No idempotency on order placement.** A retried request creates a second
  order. The fix is an `Idempotency-Key` header stored in Redis with `SET NX`,
  plus a unique index on `(user_id, idempotency_key)` as the backstop.
- **No rate limiting.**

## Inspecting things

```bash
# Postgres
docker exec -it oms-postgres psql -U oms -d oms
#   \dt                                    list tables
#   SELECT * FROM flyway_schema_history;   which migrations ran
#   SELECT name, cash_balance FROM users;  check a balance moved

# Redis
docker exec -it oms-redis redis-cli
#   KEYS *              everything stored
#   GET price:TCS       the live price, changing every second
#   TTL instrument:TCS  seconds left on the cache; -1 means no expiry
#   MONITOR             every command as it arrives
```

`MONITOR` is the quickest way to watch cache-aside working: the first request
for a symbol shows a GET then a SET, and the second shows only a GET.

## Stopping

```bash
docker compose down       # stop, keep the data
docker compose down -v    # stop and wipe the data
```

Wiping is safe. Flyway rebuilds the schema on the next start.
