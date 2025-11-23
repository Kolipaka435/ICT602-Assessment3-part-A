# Fashion E-Retail System (FERS)

## Requirements
- Java 21 (or compatible JDK)
- Apache Maven 3.9+
- No additional services are required; the embedded H2 database runs in-process.

## Setup
1. Clone or download the repository.
2. Ensure `JAVA_HOME` points to a Java 21 installation.
3. From the project root run:
   ```
   mvn compile
   ```
   This resolves dependencies, compiles sources, and creates the target directory.

## Running the Application
Execute the console application with:
```
mvn exec:java -Dexec.mainClass="org.example.Main"
```
The first launch initializes the H2 schema and seeds a default administrator account. The database files (`fers_db.mv.db` and `fers_db.trace.db`) are stored in the project root. Delete these files to reset all data for a clean run.

### Default Accounts
- Admin username: `admin`
- Admin password: `admin123`

Register customer accounts through the main menu when the application is running.

## Sample Usage Workflow
1. **Login as admin** using the default credentials.
2. **Add products** via the admin menu (provide name, description, price, and stock).
3. **Logout** and register/login as a customer.
4. **Browse products**, add them to the cart, review the cart, and proceed to checkout. Select a payment method (`ONLINE`, `CARD`, or `COD`).
5. **Logout** and return as admin to accept, reject, or deliver the created orders.
6. Customer accounts can view order history any time through the menu.

## Architecture Summary
The system follows a layered architecture:
- **Presentation layer:** `org.example.Main` hosts the console menus and input handling.
- **Business layer:** Services (`UserService`, `ProductService`, `OrderService`) encapsulate domain rules.
- **Data access layer:** DAO classes manage SQL operations for each entity.
- **Persistence layer:** `DatabaseUtil` configures JDBC access to the H2 database and creates tables on first run.

Each layer can evolve independently. Services depend on DAOs, and DAOs rely on `DatabaseUtil` for connections.

## Code Map
| Area              | Package / Class                            | Responsibility |
|-------------------|--------------------------------------------|----------------|
| Entry point       | `org.example.Main`                         | Console UI and session control |
| Domain models     | `org.example.model.*`                      | User, Product, Order, OrderItem, CartItem, Payment |
| Services          | `org.example.service.*`                    | Business logic for auth, catalog, and orders |
| Data access       | `org.example.dao.*`                        | JDBC operations (CRUD + queries) |
| Database utility  | `org.example.util.DatabaseUtil`            | Connection factory, schema creation, admin seeding |
| Build file        | `pom.xml`                                  | Maven configuration |

## Developer Notes
- Run `mvn clean` to remove compiled artifacts if needed.
- Delete `fers_db.mv.db` and `fers_db.trace.db` to reset storage.
- The application prints console notifications for key events (order accepted/rejected/delivered).
- Extend functionality by adding new service methods and corresponding DAO operations; the layered design keeps changes localized.

