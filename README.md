# CampusBite Delivery Hub

**Zambia University College of Technology (ZUT) — Object-Oriented Programming group project**

CampusBite is a small campus meal-ordering prototype. Students can browse available meals, submit orders with hostel/study-area delivery details, and view order progress. Vendors can add menu items and update order statuses.

## Technology
- Java 17
- Javalin 6 REST API
- SQLite persistence (`campusbite.db` is created automatically)
- Java Collections (`List` and `Map`) in the service layer
- HTML, CSS and JavaScript front end
- JUnit 5 tests

## Requirements
Install a JDK 17 or newer and Apache Maven. Check:
```bash
java -version
mvn -version
```

## Run
From this project folder:
```bash
mvn clean test
mvn exec:java
```
Open **http://localhost:7070** in your browser. The API health endpoint is `http://localhost:7070/api/health`.

The database file is created in the project directory on first launch. Do not delete `campusbite.db` if you want to keep the saved records.

## Main API routes
| Method | Route | Purpose |
|---|---|---|
| GET | `/api/health` | Check API |
| GET | `/api/menu` | List menu items |
| POST | `/api/menu` | Add menu item |
| DELETE | `/api/menu/{id}` | Delete menu item |
| PATCH | `/api/menu/{id}/availability` | Change availability |
| GET | `/api/orders` | List orders |
| POST | `/api/orders` | Place an order |
| DELETE | `/api/orders/{id}` | Delete an order marked Delivered |
| PATCH | `/api/orders/{id}/status` | Update order status (`PENDING`, `PREPARING`, `IN_TRANSIT`, `DELIVERED`) |
| GET | `/api/students` | List registered students |
| POST | `/api/students` | Register a student |

## Example requests
Add a menu item:
```json
{"itemName":"Chicken and rice","price":45.0,"isAvailable":true}
```
Register a student:
```json
{"name":"Example Student","phone":"0970000000","email":"student@example.com","hostelName":"Block A","roomNumber":"12"}
```
Place an order:
```json
{"studentId":1,"items":[{"foodItemId":1,"quantity":2}],"deliveryLocation":"Block A, Room 12","notes":"Call when outside"}
```
Update an order:
```json
{"status":"PREPARING"}
```
Valid statuses are `PENDING`, `PREPARING`, `IN_TRANSIT`, and `DELIVERED`.

## Important project notes
- This is an academic prototype, not a live commercial delivery service.
- It does not process online payments, provide GPS tracking, or send SMS notifications.
- Before submitting, customise the names and contributions of your actual group members, confirm your lecturer's rubric, and test on your own computer.
- Do not claim the project has been tested on your machine until you run `mvn clean test`.

## Food photos and dashboard actions

The vendor dashboard now supports uploading a food photo when creating a menu item. Supported formats are JPG, PNG, WEBP and GIF, up to 2 MB per image. The image is stored as a data URL in the SQLite `food_items.image_data` column, so it remains available after restarting the application. Existing databases are upgraded automatically when the application starts.

Vendor dashboard actions include refreshing the menu/orders, deleting menu items, changing food availability, and moving orders through Preparing, On the way, and Delivered. Customers see a live order timeline with elapsed preparation and delivery times; it refreshes automatically every 15 seconds. The vendor marks when a carrier is on the way. This prototype does not collect GPS location or track a carrier's physical movement. Delivered orders can be deleted from the vendor dashboard; earlier order statuses cannot be deleted. Deleting a menu item removes it from the customer menu while preserving the item name and price in existing order history. The customer dashboard supports adding/removing meals from a basket and placing an order with delivery details.

Run the application from the folder containing `pom.xml`:

```powershell
mvn clean test
mvn exec:java
```

Then open `http://localhost:7070`. The vendor dashboard is available after signing in through Vendor Desk; the customer dashboard is available after signing in through Customer Desk.
