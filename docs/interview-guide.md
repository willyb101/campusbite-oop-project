# CampusBite — Group Interview Preparation

## What problem does the system solve?
It organises student food orders and vendor fulfilment in one place, reducing reliance on calls and paper lists and making order status easier to follow.

## Explain the class design
- `User`: abstract parent with common identity/contact fields.
- `Student`: inherits `User`, adds hostel and room details.
- `FoodItem`: stores meal name, price, and availability.
- `Order`: links a student with ordered food items, quantities, total cost, delivery location, and status.
- `DeliveryManager`: application/service layer. Uses collections, validation, repository persistence, and order calculations.
- `InvalidOrderException`: communicates invalid quantities, unavailable meals, missing delivery details, or unknown records.

## Likely questions
1. **Where is inheritance used?** `Student extends User`.
2. **Where is encapsulation used?** Fields are private and accessed through getters; important state changes use methods.
3. **Why use SQLite?** It stores data between application restarts without requiring a separate database server.
4. **Why use Java Collections?** Lists are useful for ordered collections of menu items/order lines; maps are useful for looking up entities by ID.
5. **How is an order total calculated?** Sum each requested item's price multiplied by quantity.
6. **How do you stop invalid orders?** Validate student ID, item IDs, availability, positive quantities, and delivery location; throw `InvalidOrderException` on failure.
7. **How does a vendor update progress?** The vendor interface sends a status update to the Javalin API.
8. **What is a REST API?** HTTP endpoints that let the front end request or change application data.
9. **What persists?** Students, menu items, order headers, order lines, and status are stored in SQLite.
10. **What would you add next?** Authentication/roles, payment integration, delivery assignment, notifications, and stronger input/security controls.

## Demonstration order
1. Open the application and show the menu.
2. Register a student.
3. Add a menu item if the menu is empty.
4. Place an order with a valid delivery location.
5. Open the vendor panel and move the order through `PENDING` → `PREPARING` → `DELIVERED`.
6. Restart the app and show that saved records remain.
