## PaymentProcessor Refactoring

My choices for refactoring the `PaymentProcessor` class includes;

#### Dependency Injection

By injecting the external dependencies through the constructor we decouple the business logic from
specific implementations of infrastructure services.

Moved the data storage logic to `PaymentRepository` to avoid storing raw SQL queries in the processor.

Added `PaymentCredentials` to supply the API-key and user email to avoid having them hardcoded. 

Made the SUCCESS and FAILURE messages into enums for limitation of status message options.

Improved robustness by adding input validation and using try-catch blocks to manage failures in external services.

By injecting dependencies, we can now easily provide "Mocks" or "Stubs" during testing.
This allows us to verify the behavior of `PaymentProcessor` without executing real payments or sending real emails.

