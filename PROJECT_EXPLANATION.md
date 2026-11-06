# CampusBite Delivery Hub – Project Explanation

## 1. What this project does

CampusBite Delivery Hub is a Java-based campus food ordering system. It allows:

- Customers to register and log in
- Customers to view the available food menu
- Customers to place orders for meals
- Vendors to register and log in
- Vendors to add food items to the menu
- Vendors to view customer orders and update order status
- The system to persist data in SQLite
- A browser-based front end to interact with the application

The project is designed as a prototype for a university campus food ordering service, where students can order meals and vendors can manage their menu and orders.

---

## 2. Project structure

### Main Java package
The Java code lives under:

- src/main/java/com/zut/campusbite/

Important classes:

- Main.java – starts the Javalin web server and defines API routes
- DeliveryManager.java – business logic for students, menu items, and orders
- SqliteCampusBiteRepository.java – handles database persistence using SQLite
- CampusBiteRepository.java – repository interface defining persistent operations
- User.java – shared data for user entities
- Student.java – student-specific details
- Vendor.java – vendor-specific details (prototype class)
- FoodItem.java – represents a menu item
- Order.java – represents an order
- OrderLine.java – represents one item inside an order
- OrderRequest.java – request payload for placing an order
- StatusRequest.java – request payload for updating an order status
- InvalidOrderException.java – custom exception for invalid input
- OrderStatus.java – enum for allowed statuses

### Frontend files
The browser pages are stored in:

- src/main/resources/static/

Examples:

- index.html – home landing page
- customer/login.html – customer login page
- customer/register.html – customer registration page
- customer/dashboard.html – customer dashboard
- vendor/login.html – vendor login page
- vendor/register.html – vendor registration page
- vendor/dashboard.html – vendor dashboard
- style.css – styling for the app
- auth.js – client-side registration/login logic
- script.js – UI helpers and browser-side interactions

---

## 3. What is implemented in the project

### Customer features
- Customer registration form
- Customer login form
- Redirect to customer dashboard on successful login/registration
- Dashboard displays the available food menu
- Dashboard shows recent orders
- Validation for duplicate emails and missing passwords

### Vendor features
- Vendor registration form
- Vendor login form
- Redirect to vendor dashboard after login/registration
- Vendor dashboard allows menu item creation
- Vendor dashboard displays current menu and orders

### API features
The server exposes REST endpoints such as:

- GET /api/health
- GET /api/menu
- POST /api/menu
- PATCH /api/menu/{id}/availability
- GET /api/students
- POST /api/students
- GET /api/orders
- POST /api/orders
- PATCH /api/orders/{id}/status

These are defined in Main.java and handled through Javalin.

### Business logic features
- Student registration and duplicate prevention
- Menu validation and availability updates
- Order validation
- Order total calculation
- Status updates for order lifecycle
- SQLite-backed persistence

### Persistence layer
The project stores data in an SQLite database using JDBC. The repository handles:

- creating tables if they do not exist
- inserting students
- inserting food items
- inserting orders and order lines
- updating order status
- reading records back for display

---

## 4. What was specifically added for the login/dashboard flow

The following project functionality was added to make the app behave properly from the browser:

### auth.js
This file provides:

- registration logic for customers and vendors
- login logic for customers and vendors
- local session storage management
- redirect checks so logged-in users are sent to their dashboard
- logout support

### Dashboard behavior
The dashboard pages now:

- check whether a user is logged in
- redirect to the login page if not
- display the menu from the backend API
- show order information
- allow vendors to add food items

### Registration and login route experience
Users can now:

- register an account
- log in
- automatically move to the dashboard
- view the menu without leaving the page

This gives the app a working user flow even in a prototype environment.

---

## 5. Inheritance in the project

Inheritance is the idea that one class can inherit attributes and behavior from another class.

In this project, the key example is:

- Student extends User

### How it works
The class User contains common data such as:

- userId
- name
- phone
- email

Student inherits these fields from User and adds its own specific information:

- hostelName
- roomNumber

This means Student does not need to re-declare the common user details. It can reuse the fields and methods from User.

### Example from code
In User.java:

- protected User(int userId, String name, String phone, String email)
- setters and getters for common user properties

In Student.java:

- public class Student extends User
- Student adds hostelName and roomNumber

This demonstrates inheritance because Student is a specialized version of a general User.

---

## 6. Polymorphism in the project

Polymorphism means one interface or method can behave differently depending on the object type.

The project uses polymorphism in a few ways:

### 1. Repository interface
The project defines an interface:

- CampusBiteRepository

This interface declares methods like:

- findStudent
- saveStudent
- saveOrder
- updateOrderStatus

Then there is an implementation:

- SqliteCampusBiteRepository implements CampusBiteRepository

This is polymorphism because the same interface methods are implemented in different ways by different classes.

### 2. Method overriding
The repository implementation provides actual behavior for the repository methods.

For example:

- CampusBiteRepository declares saveStudent(Student student)
- SqliteCampusBiteRepository implements the method with SQLite logic

This is a classic form of polymorphism: one method contract, multiple object implementations.

### 3. Method behavior across objects
The same idea also appears in the use of different objects sharing common behavior, such as User subclasses or order/status handling objects.

Polymorphism helps the code stay flexible and extensible.

---

## 7. Encapsulation in the project

Encapsulation means hiding internal implementation details and exposing only the needed parts of an object.

### Example 1: User class
The fields in User are private:

- private int userId;
- private String name;
- private String phone;
- private String email;

These are accessed only through getter and setter methods.

This prevents external code from directly modifying important data without validation.

### Example 2: FoodItem class
FoodItem has private fields:

- itemId
- itemName
- price
- isAvailable

Validation is done inside setter and constructor methods. For example:

- itemName cannot be blank
- price cannot be negative

This means other classes cannot violate business rules by setting invalid values directly.

### Example 3: Order class
Order contains private fields such as:

- orderId
- studentId
- totalCost
- status

The class exposes controlled methods such as:

- getStatus()
- setStatus()
- getTotalCost()

This protects the data from invalid external changes.

Encapsulation is an important concept because it keeps the object safe and consistent.

---

## 8. Why the project is a good OOP example

The project demonstrates several strong object-oriented principles:

- Inheritance: Student extends User
- Encapsulation: private fields with validation
- Polymorphism: repository interface implemented by SQLite repository
- Abstraction: business logic is separated into service and repository classes
- Modularity: models, logic, persistence, and frontend are separated into different parts

This is exactly the type of code structure expected in an object-oriented Java project.

---

## 9. Summary

This project is a campus food ordering system with a Java backend, SQLite database, and browser front end. It allows students and vendors to register, log in, browse the menu, place orders, update status, and manage products.

The project also uses object-oriented programming principles clearly:

- Inheritance: Student inherits from User
- Polymorphism: repository interface with SQLite implementation
- Encapsulation: private state with controlled access and validation

This makes the code easier to maintain, extend, and understand.

---

## 10. Files to review for the core logic

- [src/main/java/com/zut/campusbite/Main.java](src/main/java/com/zut/campusbite/Main.java)
- [src/main/java/com/zut/campusbite/DeliveryManager.java](src/main/java/com/zut/campusbite/DeliveryManager.java)
- [src/main/java/com/zut/campusbite/SqliteCampusBiteRepository.java](src/main/java/com/zut/campusbite/SqliteCampusBiteRepository.java)
- [src/main/java/com/zut/campusbite/User.java](src/main/java/com/zut/campusbite/User.java)
- [src/main/java/com/zut/campusbite/Student.java](src/main/java/com/zut/campusbite/Student.java)
- [src/main/java/com/zut/campusbite/FoodItem.java](src/main/java/com/zut/campusbite/FoodItem.java)
- [src/main/resources/static/auth.js](src/main/resources/static/auth.js)

---

If you want, I can also create a second file named `OOP_NOTES.md` with shorter interview-style answers for inheritance, polymorphism, and encapsulation that you can use directly in class or assignment submission.
