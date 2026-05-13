# Shopping Cart Interview Prep

This file summarizes interview-ready explanations, likely technical questions, and pair-programming exercises for this shopping cart project.

## Project Summary

This project implements a simple shopping cart library.

Core capabilities:

- Add product by name and quantity
- Retrieve product price through `PriceStrategy`
- Aggregate quantity for the same product
- Calculate subtotal
- Apply cart-level percentage discount
- Calculate 12.5% tax
- Calculate total payable
- Handle invalid input and API failures

Main classes:

- `ShoppingCart`: cart state and business logic
- `PriceStrategy`: abstraction for price retrieval
- `ApiPriceStrategy`: retrieves prices from the Equal Experts price API
- `TaxStrategy`: abstraction for tax calculation
- `DefaultTaxStrategy`: calculates 12.5% tax
- `Product`: product value object
- `CartItem`: product plus quantity
- `PriceRetrievalException`: runtime exception for price lookup failure

## High-Level Design Explanation

Interview answer:

> I kept the design intentionally simple. `ShoppingCart` owns cart behavior and state. It does not know how prices are fetched or how tax is calculated. Those behaviors are injected through `PriceStrategy` and `TaxStrategy`, which makes the cart easy to test and keeps API integration separate from business logic.

## Why Use Strategy Pattern?

Question:

> Why did you use `PriceStrategy` and `TaxStrategy`?

Answer:

> I used small strategy interfaces because price retrieval and tax calculation are behaviors that can vary. Price could come from an API, mock, file, or database. Tax rules could also change. Injecting these dependencies keeps `ShoppingCart` focused on cart behavior and improves testability.

Important note:

> I avoided adding extra layers like repositories, controllers, services, persistence, or APIs because the assignment explicitly values simplicity.

## Why BigDecimal?

Question:

> Why use `BigDecimal` instead of `double`?

Answer:

> Money should not be represented with floating-point types because `double` can introduce precision errors. `BigDecimal` gives exact decimal arithmetic and lets us explicitly control rounding.

Example:

```java
new BigDecimal("2.52")
```

Better than:

```java
new BigDecimal(2.52)
```

Because string construction avoids binary floating-point precision issues.

## Rounding

Question:

> Why use `RoundingMode.HALF_UP`?

Answer:

> `HALF_UP` is common financial rounding and matches the sample result from the assignment. The sample tax is `15.02 * 0.125 = 1.8775`, which rounds to `1.88`.

Possible follow-up:

> The requirement says "rounded up where required." If interpreted literally, `RoundingMode.CEILING` could be used, but since the sample matches normal financial rounding, I used `HALF_UP`. In a real project, I would clarify the exact rounding rule.

## Cart Calculation Flow

Without discount:

```text
subtotal = sum(item price * quantity)
tax = subtotal * 12.5%
total = subtotal + tax
```

With discount:

```text
discount = subtotal * discountPercentage / 100
discountedSubtotal = subtotal - discount
tax = discountedSubtotal * 12.5%
total = discountedSubtotal + tax
```

Interview answer:

> I apply discount before tax because tax is usually calculated on the payable amount after discount. This is a business rule, so I would confirm it with the product owner if it were not specified.

## Why Some Fields Are Final

Question:

> Why are some fields final and some not?

Answer:

> I use `final` for fields whose reference should not change after construction. For example, `priceStrategy`, `taxStrategy`, and `items` should always refer to the same dependencies or collection. Fields that represent changing state, like `quantity` or `discountPercentage`, are not final.

Important detail:

```java
private final Map<String, CartItem> items = new HashMap<>();
```

This means the map reference cannot be replaced, but its contents can still change.

## Immutability

Question:

> Is the cart immutable?

Answer:

> No. `ShoppingCart` is mutable because adding products changes cart state. `Product` is immutable because its fields are final and there are no setters. `CartItem` is partially mutable because quantity can increase.

Question:

> Is `getItems()` fully immutable?

Answer:

> It returns an unmodifiable map, so callers cannot add or remove entries from the map. However, the `CartItem` objects inside are still objects. A stronger design would return a read-only snapshot or DTO, but that may be more complexity than needed for this assignment.

## API Integration

Question:

> What does `ApiPriceStrategy` do?

Answer:

> It builds the product URL, calls the Equal Experts price API using Java `HttpClient`, validates the HTTP status, parses the JSON response using Gson, and returns the price as `BigDecimal`.

Question:

> What happens if API returns non-200?

Answer:

> The code throws `PriceRetrievalException`.

Question:

> What happens if price is missing?

Answer:

> The parsed response is validated. If the response is null or price is null, `PriceRetrievalException` is thrown.

Question:

> Why wrap exceptions in `PriceRetrievalException`?

Answer:

> It gives callers a domain-specific failure instead of exposing low-level exceptions like `IOException`, `InterruptedException`, JSON parsing errors, or invalid URI errors.

## Testing Strategy

Question:

> What types of tests are used?

Answer:

> `ShoppingCartTest` tests cart behavior using fake price data. `DefaultTaxStrategyTest` directly tests tax calculations. `ApiPriceStrategyTest` validates API behavior. Mockito-based tests show how dependencies can be mocked for deterministic unit tests.

## Mockito Usage

Question:

> Why use Mockito for `ShoppingCart`?

Answer:

> `ShoppingCart` depends on collaborators: `PriceStrategy` and `TaxStrategy`. Mockito lets me isolate cart behavior by controlling those dependencies and verifying interactions.

Question:

> Why not use Mockito for `DefaultTaxStrategy`?

Answer:

> `DefaultTaxStrategy` has no external dependency. It is a pure calculation, so direct input-output tests are clearer and more valuable.

Question:

> Should `ApiPriceStrategy` be tested with Mockito?

Answer:

> It can be tested with Mockito by mocking `HttpClient` and `HttpResponse`, but this is easier if `HttpClient` is injected. Since the production class currently creates its own `HttpClient`, reflection is needed in tests if we do not want to change production code. For cleaner design, I would prefer constructor injection.

## Real API Tests vs Mock Tests

Question:

> Should unit tests call the real API?

Answer:

> Usually no. Unit tests should be fast, deterministic, and offline. Real API tests can fail due to network, API downtime, or changed data. I would keep real API tests as integration tests, separated from normal unit tests.

Recommended setup:

```text
Unit tests:
- ShoppingCart with mocked/fake PriceStrategy
- DefaultTaxStrategy direct calculation tests
- ApiPriceStrategy with mocked HttpClient or fake local server

Integration tests:
- Optional real Equal Experts API test, tagged separately
```

## TDD Explanation

Question:

> What is Test Driven Development?

Answer:

> TDD is a development approach where I write a failing test first, then write the simplest production code to make it pass, and finally refactor while keeping tests green.

Cycle:

```text
Red -> Green -> Refactor
```

Discount example:

> First I wrote tests for default discount, percentage discount, and invalid discount. The tests failed because the methods did not exist. Then I added `applyDiscount` and `getDiscount`, updated tax and total calculation, and reran the full test suite.

## Discount Functionality

Question:

> How does discount work?

Answer:

> Discount is cart-level percentage discount. It is applied to subtotal before tax. The discount percentage must be between 0 and 100.

Example:

```text
Subtotal = 5.04
Discount = 10%
Discount amount = 0.50
Subtotal after discount = 4.54
Tax = 0.57
Total = 5.11
```

Question:

> Why use percentage discount instead of flat discount?

Answer:

> Percentage discount is simple and common. If requirements asked for flat discount, coupon codes, item-level discount, or buy-one-get-one offers, I would model discount as a separate strategy.

Question:

> What validation is needed for discount?

Answer:

> Null discount, negative discount, and discount greater than 100 should be rejected.

## Expected Technical Questions

### 1. Why does adding same product aggregate quantity?

Answer:

> The cart stores items in a map keyed by product name. If the product already exists, it increases the existing quantity instead of adding a duplicate entry.

### 2. Should product names be case-sensitive?

Answer:

> Currently yes. The API expects specific lowercase product names. If required, I would normalize input using `trim().toLowerCase()` before lookup.

### 3. Why validate product name before calling API?

Answer:

> It avoids unnecessary API calls and gives immediate feedback for invalid input.

### 4. Why validate quantity?

Answer:

> Zero or negative quantity does not make sense for adding a product. Invalid quantities should fail fast.

### 5. Should adding same product call API again?

Answer:

> Current behavior calls the price strategy each time before aggregation. Whether this is correct depends on business rules. If cart price should be fixed from first add, we can avoid repeated price lookup. If price should always be current, repeated lookup is acceptable.

### 6. What if API price changes between adds?

Answer:

> With the current implementation, the existing `CartItem` keeps the original `Product` and price when quantity is aggregated. The second fetched price is not used if the item already exists. If price changes matter, we should define whether to update price, keep first price, or store line items separately.

### 7. Is this code thread-safe?

Answer:

> No. The cart uses mutable state and a `HashMap`. The README assumes single-threaded usage. For concurrent use, I would add synchronization or use a concurrent structure, but only if required.

### 8. Why use unchecked runtime exception for price retrieval?

Answer:

> It keeps the cart API simple. Price retrieval failure is usually not recoverable at the low-level cart operation. In a larger app, this could be handled at the boundary layer and mapped to user-facing error behavior.

### 9. Why use Gson?

Answer:

> It is a lightweight JSON parser and enough for this simple API response.

### 10. Why no controller, database, or web API?

Answer:

> The assignment explicitly says not to submit an app or add unnecessary architectural layers. This is a library-style solution focused on the required behavior.

### 11. Why `Collections.unmodifiableMap`?

Answer:

> It prevents external callers from directly changing the cart map. Cart state should be changed through cart methods.

### 12. What would you improve?

Answer:

> I would separate real API tests from unit tests, make `HttpClient` injectable for cleaner testing, possibly return immutable cart snapshots, and clarify rounding and discount business rules.

### 13. Why does `getTotal()` call `getTax()`?

Answer:

> It avoids duplicating tax calculation logic. For larger systems, a `CartSummary` object could calculate subtotal, discount, tax, and total once to avoid repeated computation.

### 14. Are repeated calculations a problem?

Answer:

> Not for this small cart. The cart has few items and calculations are cheap. If performance became a concern, I would calculate a summary once or cache totals carefully.

### 15. What if `PriceStrategy` returns null?

Answer:

> Currently that could lead to a later failure. A defensive improvement would validate returned price in `ShoppingCart` or guarantee non-null price through `PriceStrategy` contract.

### 16. What if price is negative?

Answer:

> Current code does not validate negative price. Since prices come from trusted API, it may be acceptable for this assignment. A robust improvement would reject null or negative prices.

### 17. Why package by responsibility?

Answer:

> Classes are grouped into `model`, `pricing`, `tax`, `service`, and `common`, which keeps responsibilities discoverable without adding unnecessary layers.

### 18. Why `PriceResponse` has no setters?

Answer:

> Gson can populate private fields reflectively. The production code only needs getters.

### 19. Why test invalid API product names?

Answer:

> It verifies API failure handling and ensures invalid or unsupported product names are converted into `PriceRetrievalException`.

### 20. Why parameterized tests?

Answer:

> Parameterized tests are useful when the same behavior needs to be verified for multiple inputs. For example, checking all known products and expected prices.

## Expected Pair-Programming Tasks

### Task 1: Add Remove Product

Requirement:

```java
cart.removeProduct("cornflakes");
```

Expected approach:

1. Add test for removing existing product.
2. Add test for removing missing product.
3. Decide whether missing product should throw or do nothing.
4. Implement in `ShoppingCart`.

Possible answer:

> I would choose no-op for missing product unless requirement says otherwise, because removing something absent can be treated as idempotent.

### Task 2: Update Quantity

Requirement:

```java
cart.updateQuantity("cornflakes", 3);
```

Rules to clarify:

- Should quantity zero remove item?
- Should missing product throw?
- Should update fetch latest price?

Good interview response:

> I would clarify whether update means replacing quantity or adding quantity. Current `addProduct` increments quantity, so `updateQuantity` should probably set the quantity directly.

### Task 3: Add Flat Discount

Requirement:

```java
cart.applyFlatDiscount(new BigDecimal("5.00"));
```

Clarify:

- Can discount exceed subtotal?
- Is tax calculated after discount?
- Can flat and percentage discounts both apply?

### Task 4: Add Item-Level Discount

Requirement:

```java
cart.applyDiscount("cornflakes", new BigDecimal("10"));
```

Expected design:

> If discount rules become complex, introduce a `DiscountStrategy`. For one simple discount, keep it simple inside cart logic.

### Task 5: Add Cart Summary

Requirement:

```java
CartSummary summary = cart.getSummary();
```

Fields:

```text
items
subtotal
discount
tax
total
```

Why useful:

> It avoids callers making multiple getter calls and gives one consistent snapshot of cart state.

### Task 6: Make API Tests Offline

Expected answer:

> I would avoid real network calls in unit tests. I can mock `HttpClient`, use a fake local HTTP server, or refactor `ApiPriceStrategy` to accept a small HTTP abstraction.

### Task 7: Inject HttpClient

Requirement:

> Make `ApiPriceStrategy` easier to test.

Good solution:

```java
public ApiPriceStrategy() {
    this(HttpClient.newHttpClient());
}

ApiPriceStrategy(HttpClient httpClient) {
    this.httpClient = httpClient;
}
```

Answer:

> Constructor injection improves testability without changing runtime behavior.

### Task 8: Normalize Product Names

Requirement:

```java
cart.addProduct(" Cornflakes ", 1);
```

Expected behavior:

```text
cornflakes
```

Implementation:

```java
String normalizedName = name.trim().toLowerCase();
```

Clarify:

> I would confirm whether display name should preserve original casing.

### Task 9: Add Product Price Validation

Requirement:

> Reject null or negative prices returned by `PriceStrategy`.

Test first:

```java
when(priceStrategy.getPrice("cornflakes")).thenReturn(null);
assertThrows(PriceRetrievalException.class, ...);
```

Clarify exception type:

> Could use `IllegalStateException` or a domain exception. Since price retrieval failed contract validation, `PriceRetrievalException` is reasonable.

### Task 10: Separate Integration Tests

Expected Gradle idea:

```java
@Tag("integration")
```

Then configure normal test task to exclude integration tests.

Interview answer:

> This keeps normal unit tests fast and reliable while still allowing real API verification when explicitly requested.

## Code Review Questions

Question:

> What concerns do you see in this code?

Answer:

> Main concerns are real API tests in the normal test suite, hardcoded `HttpClient` making API tests harder, returned cart items not being fully immutable, and unclear business rules around rounding and discount application.

Question:

> Is there overengineering?

Answer:

> The current strategy interfaces are acceptable because they improve testability and represent varying behavior. I would avoid adding more layers unless new requirements justify them.

Question:

> Is the README good enough?

Answer:

> It explains design, assumptions, AI usage, and test command. I would fix encoding artifacts and typos before final submission.

## Common Java Questions From This Code

### What is a lambda?

Example:

```java
() -> priceStrategy.getPrice(productName)
```

Answer:

> It is a short implementation of a functional interface. In JUnit `assertThrows`, the lambda represents executable code that JUnit runs and checks for an exception.

### What is a functional interface?

Answer:

> An interface with exactly one abstract method. Lambdas can be used to implement functional interfaces.

### What does `assertThrows` do?

Answer:

> It runs the provided executable block and passes if the expected exception type is thrown.

### What is Mockito `when(...).thenReturn(...)`?

Answer:

> It defines behavior for a mock. When the mocked method is called with matching arguments, Mockito returns the configured value.

### What does `verify(...)` do?

Answer:

> It checks that a mock method was called as expected.

### What is `@BeforeEach`?

Answer:

> It runs before each test method, usually to create fresh test objects or common setup.

### Why compare BigDecimal with `compareTo`?

Answer:

> `BigDecimal.equals` checks value and scale, so `2.50` and `2.5` are not equal. `compareTo` compares numeric value.

## Strong Final Interview Pitch

Use this if asked to summarize your solution:

> I built a simple library-style shopping cart solution. The cart supports adding products, aggregating quantities, calculating subtotal, discount, tax, and total. I used `BigDecimal` for monetary accuracy and explicit rounding. I separated price retrieval and tax calculation behind small interfaces so the cart logic is easy to test. The API client validates status codes and response payloads and throws a domain exception for failures. Tests cover cart calculations, validation, tax calculation, API handling, and Mockito-based dependency isolation. I kept the design intentionally small because the assignment asks for simplicity rather than unnecessary architecture.

