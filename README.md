# CampusBite Delivery Hub

**Zambia University College of Technology (ZUT) — Object-Oriented Programming Group Project**

CampusBite is a campus meal-ordering prototype that allows students to browse available meals, place orders with hostel or study-area delivery details, and monitor order progress. Vendors can manage menu items, view customer orders and update order statuses.

## 1. THE PROBLEM

Students at Zambia University College of Technology (ZUT) may experience difficulties accessing meals conveniently while attending lectures, studying or staying in campus hostels. Food vendors may also struggle to manage customer orders, track order progress and maintain updated menus manually.

CampusBite Delivery Hub addresses these challenges by providing a digital platform where students can browse available meals, place orders and monitor delivery progress, while vendors can manage food items and update order statuses.

## 2. TECHNOLOGY STACK

- Java 17
- Javalin 6 REST API
- SQLite database for data persistence
- Java Collections (`List` and `Map`) in the service layer
- HTML, CSS and JavaScript front end
- JUnit 5 testing framework
- Apache Maven for dependency management and building

## 3. HOW TO RUN

Follow these steps to install and run the CampusBite Delivery Hub application.

### Prerequisites

Install the following software:

- Java Development Kit (JDK) 17 or newer.
- Apache Maven.
- A modern web browser.

Verify that Java and Maven are installed by running:

```bash
java -version
mvn -version
```

### Running the application

**Step 1: Open the project**

Open the `CampusBite-Delivery-Hub-Updated` folder in Visual Studio Code or your preferred Java IDE.

**Step 2: Open the terminal**

Make sure the terminal is in the project directory containing `pom.xml`.

**Step 3: Run the tests**

Execute:

```bash
mvn clean test
```

This compiles the project and runs the JUnit 5 tests.

**Step 4: Start the application**

Execute:

```bash
mvn exec:java
```

**Step 5: Open the application**

Open your web browser and visit:

http://localhost:7070

The API health endpoint is available at:

http://localhost:7070/api/health

**Database information:** The SQLite database file (`campusbite.db`) is automatically created in the project directory when the application starts. Do not delete this file if you want to preserve saved records.

## 4. YOUR CLASSES

The project uses Object-Oriented Programming principles to separate responsibilities between different parts of the application.

The Java classes are organized according to their responsibilities:

| Class Category | Responsibility |
|---|---|
| Model Classes | Represent application data, including students, food items and customer orders. |
| Service Classes | Handle application business logic, validation, calculations and order management. |
| Repository Classes | Manage database operations and store and retrieve application records using SQLite. |
| Main Application Class | Configures and starts the Javalin server and registers the REST API endpoints. |
| Test Classes | Test application functionality and verify that important operations work correctly. |

The system uses Java Collections, including `List` and `Map`, to manage application data within the service layer.

## 5. MAIN API ROUTES

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/health` | Check API status |
| GET | `/api/menu` | List menu items |
| POST | `/api/menu` | Add a menu item |
| DELETE | `/api/menu/{id}` | Delete a menu item |
| PATCH | `/api/menu/{id}/availability` | Change food availability |
| GET | `/api/orders` | List customer orders |
| POST | `/api/orders` | Place an order |
| DELETE | `/api/orders/{id}` | Delete an order marked Delivered |
| PATCH | `/api/orders/{id}/status` | Update order status |
| GET | `/api/students` | List registered students |
| POST | `/api/students` | Register a student |

### Order statuses

The system supports the following order statuses:

- `PENDING`
- `PREPARING`
- `IN_TRANSIT`
- `DELIVERED`

## 6. EXAMPLE API REQUESTS

### Add a menu item

```json
{
  "itemName": "Chicken and rice",
  "price": 45.0,
  "isAvailable": true
}
```

### Register a student

```json
{
  "name": "Example Student",
  "phone": "0970000000",
  "email": "student@example.com",
  "hostelName": "Block A",
  "roomNumber": "12"
}
```

### Place an order

```json
{
  "studentId": 1,
  "items": [
    {
      "foodItemId": 1,
      "quantity": 2
    }
  ],
  "deliveryLocation": "Block A, Room 12",
  "notes": "Call when outside"
}
```

### Update an order

```json
{
  "status": "PREPARING"
}
```

## 7. SYSTEM FEATURES

### Student Dashboard

- Browse available food items.
- Add meals to a shopping basket.
- Remove meals from the basket.
- Place food orders.
- Provide hostel and room delivery details.
- View order progress through a live order timeline.

### Vendor Dashboard

- Add new food items to the menu.
- Upload food photographs.
- Delete menu items.
- Change food availability.
- View customer orders.
- Update order statuses.
- Refresh menu and order information.
- Delete orders marked as Delivered.

### Food Image Upload

The vendor dashboard supports uploading food photographs in the following formats:

- JPG
- PNG
- WEBP
- GIF

The maximum image size is 2 MB per image.

Images are stored as data URLs in the SQLite `food_items.image_data` column, allowing them to remain available after restarting the application. Existing databases are upgraded automatically when the application starts.

### Order Tracking

The customer dashboard displays an order timeline with elapsed preparation and delivery times. It refreshes automatically every 15 seconds.

The vendor marks when a carrier is on the way. This prototype does not collect GPS coordinates or track the carrier's physical movement.

Deleting a menu item removes it from the customer menu while preserving the item name and price in existing order history.

## 8. PROJECT LIMITATIONS

CampusBite Delivery Hub is an academic prototype and not a live commercial delivery service.

The current system:

- Does not process online payments.
- Does not provide GPS tracking.
- Does not send SMS notifications.
- Does not track a carrier's physical movement.
- Uses local SQLite storage rather than a remote production database.

## 9. GROUP MEMBERS

The following students contributed to the development of the CampusBite Delivery Hub project.

| No. | Full Name | Student ID |
|:---:|---|:---:|
| 1 | MAKULE CHILUPULA | 2410102 |
| 2 | WILLIAM GONDWE | 2410426 |
| 3 | KEZIA CHIMIA | 2410284 |
| 4 | KONDWANI CHILONGO | 2410370 |
| 5 | SAVIOUR MWAPE | 2410082 |

## 10. ACADEMIC PROJECT INFORMATION

**Institution:** Zambia University College of Technology (ZUT)

**Project Name:** CampusBite Delivery Hub

**Course:** Object-Oriented Programming

**Programming Language:** Java 17

**Backend Framework:** Javalin 6

**Database:** SQLite

**Project Type:** Group Academic Project

---

**CampusBite Delivery Hub — Making campus food ordering easier through technology.**
