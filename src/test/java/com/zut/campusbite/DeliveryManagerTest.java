package com.zut.campusbite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryManagerTest {
    @TempDir Path tempDir;

    @Test void studentInheritsCommonUserDetails() {
        Student student = new Student(1, "Mwamba", "0970000000", "m@example.com", "Block A", "12");
        assertEquals("Mwamba", student.getName());
        assertEquals("Block A", student.getHostelName());
    }

    @Test void orderRejectsZeroQuantity() {
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(tempDir.resolve("test.db").toString()));
        Student student = manager.registerStudent("Mwamba", "0970000000", "m@example.com", "Block A", "12");
        FoodItem food = manager.getMenu().get(0);
        OrderRequest request = new OrderRequest();
        request.studentId = student.getUserId();
        request.deliveryLocation = "Block A, Room 12";
        request.items = java.util.List.of(new OrderRequest.ItemRequest());
        request.items.get(0).foodItemId = food.getItemId();
        request.items.get(0).quantity = 0;
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> manager.placeOrder(request));
        assertEquals("Every quantity must be greater than zero", exception.getMessage());
    }

    @Test void validOrderCalculatesTotalAndPersistsStatus() {
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(tempDir.resolve("valid.db").toString()));
        Student student = manager.registerStudent("Mwamba", "0970000000", "m@example.com", "Block A", "12");
        FoodItem food = manager.getMenu().get(0);
        OrderRequest request = new OrderRequest();
        request.studentId = student.getUserId();
        request.deliveryLocation = "Block A, Room 12";
        OrderRequest.ItemRequest line = new OrderRequest.ItemRequest();
        line.foodItemId = food.getItemId(); line.quantity = 2;
        request.items = java.util.List.of(line);
        Order order = manager.placeOrder(request);
        assertEquals(food.getPrice() * 2, order.getTotalCost(), 0.001);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1, manager.getOrders().size());
    }

    @Test void foodImagePersistsAndAppearsWhenMenuIsReloaded() {
        String databasePath = tempDir.resolve("food-image.db").toString();
        String imageData = "data:image/png;base64,iVBORw0KGgo=";
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(databasePath));

        FoodItem saved = manager.addFoodItem("Test meal", 25, true, imageData);
        DeliveryManager reloadedManager = new DeliveryManager(new SqliteCampusBiteRepository(databasePath));

        FoodItem reloaded = reloadedManager.getMenu().stream()
                .filter(item -> item.getItemId() == saved.getItemId())
                .findFirst()
                .orElseThrow();
        assertEquals(imageData, reloaded.getImageData());
    }

    @Test void deletingFoodRemovesItFromMenuAndPreservesOrderHistory() {
        String databasePath = tempDir.resolve("delete-food.db").toString();
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(databasePath));
        Student student = manager.registerStudent("Mwamba", "0970000000", "m@example.com", "Block A", "12");
        FoodItem food = manager.addFoodItem("Test meal", 25, true);
        OrderRequest request = new OrderRequest();
        request.studentId = student.getUserId();
        request.deliveryLocation = "Block A, Room 12";
        OrderRequest.ItemRequest line = new OrderRequest.ItemRequest();
        line.foodItemId = food.getItemId();
        line.quantity = 1;
        request.items = java.util.List.of(line);
        manager.placeOrder(request);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE order_lines_legacy (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, food_item_id INTEGER NOT NULL, item_name TEXT NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), unit_price REAL NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(id))");
            statement.execute("INSERT INTO order_lines_legacy SELECT id,order_id,food_item_id,item_name,quantity,unit_price FROM order_lines");
            statement.execute("DROP TABLE order_lines");
            statement.execute("ALTER TABLE order_lines_legacy RENAME TO order_lines");
        } catch (java.sql.SQLException e) {
            throw new AssertionError("Could not prepare legacy order schema", e);
        }

        DeliveryManager migratedManager = new DeliveryManager(new SqliteCampusBiteRepository(databasePath));
        migratedManager.deleteFoodItem(food.getItemId());

        assertTrue(migratedManager.getMenu().stream().noneMatch(item -> item.getItemId() == food.getItemId()));
        assertEquals("Test meal", migratedManager.getOrders().get(0).getItems().get(0).getItemName());
        assertThrows(InvalidOrderException.class, () -> migratedManager.deleteFoodItem(food.getItemId()));
    }

    @Test void onlyDeliveredOrdersCanBeDeleted() {
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(tempDir.resolve("delete-order.db").toString()));
        Student student = manager.registerStudent("Mwamba", "0970000000", "m@example.com", "Block A", "12");
        FoodItem food = manager.getMenu().get(0);
        OrderRequest request = new OrderRequest();
        request.studentId = student.getUserId();
        request.deliveryLocation = "Block A, Room 12";
        OrderRequest.ItemRequest line = new OrderRequest.ItemRequest();
        line.foodItemId = food.getItemId();
        line.quantity = 1;
        request.items = java.util.List.of(line);
        Order order = manager.placeOrder(request);

        assertThrows(InvalidOrderException.class, () -> manager.deleteDeliveredOrder(order.getOrderId()));
        assertEquals(1, manager.getOrders().size());

        manager.updateStatus(order.getOrderId(), "DELIVERED");
        manager.deleteDeliveredOrder(order.getOrderId());

        assertTrue(manager.getOrders().isEmpty());
        assertThrows(InvalidOrderException.class, () -> manager.deleteDeliveredOrder(order.getOrderId()));
    }

    @Test void orderTrackerPersistsPreparationAndDeliveryStageTimes() {
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(tempDir.resolve("order-tracker.db").toString()));
        Student student = manager.registerStudent("Mwamba", "0970000000", "m@example.com", "Block A", "12");
        FoodItem food = manager.getMenu().get(0);
        OrderRequest request = new OrderRequest();
        request.studentId = student.getUserId();
        request.deliveryLocation = "Block A, Room 12";
        OrderRequest.ItemRequest line = new OrderRequest.ItemRequest();
        line.foodItemId = food.getItemId();
        line.quantity = 1;
        request.items = java.util.List.of(line);
        Order order = manager.placeOrder(request);

        manager.updateStatus(order.getOrderId(), "PREPARING");
        Order preparing = manager.getOrders().get(0);
        assertNotNull(preparing.getPreparingAt());
        assertNull(preparing.getInTransitAt());

        manager.updateStatus(order.getOrderId(), "IN_TRANSIT");
        Order inTransit = manager.getOrders().get(0);
        assertNotNull(inTransit.getInTransitAt());
        assertTrue(java.time.Instant.parse(inTransit.getInTransitAt())
                .compareTo(java.time.Instant.parse(inTransit.getPreparingAt())) >= 0);

        manager.updateStatus(order.getOrderId(), "DELIVERED");
        Order delivered = manager.getOrders().get(0);
        assertNotNull(delivered.getDeliveredAt());
        assertTrue(java.time.Instant.parse(delivered.getDeliveredAt())
                .compareTo(java.time.Instant.parse(delivered.getInTransitAt())) >= 0);
    }
}
