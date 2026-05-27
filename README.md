# 🧱 Architectural Proof of Concept: Migrating Monolithic Backend to Spring Modulith

---

## 📖 1. Introduction and Motivation
The original backend for my bachelor thesis, Booknest, was built using a standard layered architecture (controllers, services, repositories). The application serves as an online library platform where users can explore an extensive book catalog, easily filter titles by categories, borrow available books, and share their reading experiences through ratings and reviews. 

While the layered structure was easy to set up initially, the business domains became tightly coupled as the application grew. For example, the authentication flow was directly calling the catalog’s database to save author profiles, and deleting a book required reaching directly into the lending and review tables to cascade the deletions. 

To organize the codebase around actual business capabilities without taking on the heavy infrastructure overhead of microservices, we initiated this Proof of Concept (PoC) to refactor my bachelor thesis into a modular monolith using **Spring Modulith**. Spring Modulith is an extension of the Spring Boot framework that helps developers build well-structured applications by defining strict, logical boundaries between different business domains. It provides built-in tools for event-driven communication and architectural testing, ensuring the system remains maintainable while still being deployed as a single unit.

---

## 🏗️ 2. The Modular Redesign
We restructured the codebase by slicing it into distinct business domains, alongside a shared package for global components:

- 🔐 **`identity`**: Handles user registration, authentication, and email verification.
- 📚 **`catalog`**: Manages the core inventory (books) and author profiles.
- 🔄 **`lending`**: Manages the business logic of borrowing, returning, and tracking due dates.
- ⭐ **`review`**: Handles user feedback and ratings.
- 🛠️ **`common`**: A shared utility package containing global cross-cutting concerns, such as the `APIResponse` wrapper, `ErrorDTO` (which is included in the API response), and `JwtUtils` for security.

> **The Golden Rule of Visibility:** Every business module was split into a public root package (the API) and an `internal` sub-package. The framework strictly forbids any module from importing classes from another module's `internal` package. This forces all communication to happen through explicitly defined contracts.

---

## 🔗 3. Decoupling the Domains
To eliminate the tangled dependencies, we implemented three key communication strategies:

### 📡 A. Asynchronous Event Publication (The Observer Pattern)
We replaced hard dependencies with Spring Application Events. 
- *Example:* When an author registers, the `identity` module no longer saves data to the `AuthorRepository`. Instead, it simply publishes an `AuthorRegisteredEvent`. The `catalog` module listens for this event asynchronously and handles the database insertion.
- *Example:* When a book is deleted, the `catalog` broadcasts a `BookDeletedEvent`. The `lending` and `review` modules catch this event and independently delete their associated records, completely removing the need for cross-domain repository injections.

### 💾 B. The Transactional Outbox Pattern
Relying on events introduces the risk of data loss if a listener fails. To guarantee "at-least-once" delivery, we integrated Spring Modulith's **JDBC Outbox** feature. When an event like `AuthorRegisteredEvent` is fired, it is simultaneously saved to an `event_publication` table in our local database within the same transaction. If the `catalog` module crashes while processing the author profile, the framework automatically retries the event later.

### 📞 C. Synchronous API Calls
When a module simply needs to read data or enforce a rule, we used synchronous method calls exposed through the public API. For instance, when a user tries to borrow a book, the `lending` module calls the `catalog` module's public `BookService` to verify that `availableCopies > 0`. If the book is out of stock, the catalog throws an exception, and the transaction safely rolls back. 

---

## 🛡️ 4. Architectural Verification and Automated Documentation
The most critical part of this PoC is proving that our modular boundaries are strictly enforced by the compiler and test suite, rather than just being informal conventions. 

Using **ArchUnit** via Spring Modulith’s testing module, we implemented a test that verifies the entire dependency graph (`ApplicationModules.of(BackendApplication.class).verify()`). If a future developer tries to bypass the rules (e.g., by making the `lending` module talk directly to the internal `BookRepository`), this test will immediately fail the CI/CD build.

Additionally, this exact same test dynamically generates our **C4 component diagrams** and **PlantUML files** by inspecting the live code, ensuring our architectural documentation is never out of date.

![diagram](https://github.com/GotaSeptimiuAndrei/spring-modulith/blob/main/components-SpringModulithApplication.png?raw=true)
---

## 🎉 5. Conclusion
By migrating to Spring Modulith, Booknest now possesses the clean, decoupled boundaries of a microservices architecture while retaining the simplicity, performance, and easy local development of a standard monolith.
