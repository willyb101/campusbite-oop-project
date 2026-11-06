package com.zut.campusbite;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliveryManager {
    private final CampusBiteRepository repository;
    // Collections are used as an in-memory working view; SQLite remains the persistent source.
    private final Map<Integer, Student> studentsById = new HashMap<>();
    private final Map<Integer, FoodItem> menuById = new LinkedHashMap<>();
    private final List<Order> recentOrders = new ArrayList<>();

    public DeliveryManager(CampusBiteRepository repository) {
        this.repository = repository;
        reloadCollections();
    }
    public synchronized void refreshCollections() {
        reloadCollections();
    }
    private void reloadCollections() {
        studentsById.clear(); repository.findStudents().forEach(s -> studentsById.put(s.getUserId(), s));
        menuById.clear(); repository.findFoodItems().forEach(f -> menuById.put(f.getItemId(), f));
        recentOrders.clear(); recentOrders.addAll(repository.findOrders());
    }
    public List<Student> getStudents() { refreshCollections(); return new ArrayList<>(studentsById.values()); }
    public List<FoodItem> getMenu() { refreshCollections(); return new ArrayList<>(menuById.values()); }
    public List<Order> getOrders() { refreshCollections(); return new ArrayList<>(recentOrders); }

    public Student registerStudent(String name, String phone, String email, String hostel, String room) {
        try {
            if (email == null || email.isBlank()) {
                throw new InvalidOrderException("Email address is required");
            }

            boolean emailExists = repository.findStudents().stream()
                    .anyMatch(student -> student.getEmail().equalsIgnoreCase(email.trim()));

            if (emailExists) {
                throw new InvalidOrderException("An account with this email already exists");
            }

            Student student = new Student(0, name, phone, email.trim(), hostel, room);
            Student saved = repository.saveStudent(student);
            studentsById.put(saved.getUserId(), saved);
            return saved;
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderException(e.getMessage());
        }
    }
    public FoodItem addFoodItem(String name, double price, boolean available) {
        return addFoodItem(name, price, available, null);
    }
    public FoodItem addFoodItem(String name, double price, boolean available, String imageData) {
        try {
            FoodItem item = new FoodItem(0, name, price, available, imageData);
            FoodItem saved = repository.saveFoodItem(item);
            menuById.put(saved.getItemId(), saved);
            return saved;
        } catch (IllegalArgumentException e) { throw new InvalidOrderException(e.getMessage()); }
    }
    public void deleteFoodItem(int id) {
        if (!repository.deleteFoodItem(id)) throw new InvalidOrderException("Food item not found");
        refreshCollections();
    }
    public void setAvailability(int id, boolean available) {
        if (repository.findFoodItem(id).isEmpty()) throw new InvalidOrderException("Food item not found");
        repository.updateAvailability(id, available);
        refreshCollections();
    }
    public Order placeOrder(OrderRequest request) {
        if (request == null) throw new InvalidOrderException("Order body is required");
        Student student = repository.findStudent(request.studentId).orElseThrow(() -> new InvalidOrderException("Register the student before ordering"));
        if (request.items == null || request.items.isEmpty()) throw new InvalidOrderException("Choose at least one food item");
        if (request.deliveryLocation == null || request.deliveryLocation.isBlank()) throw new InvalidOrderException("Delivery location is required");
        List<OrderLine> lines = new ArrayList<>();
        double total = 0;
        for (OrderRequest.ItemRequest requested : request.items) {
            if (requested == null || requested.quantity <= 0) throw new InvalidOrderException("Every quantity must be greater than zero");
            FoodItem item = repository.findFoodItem(requested.foodItemId).orElseThrow(() -> new InvalidOrderException("Food item " + requested.foodItemId + " was not found"));
            if (!item.isAvailable()) throw new InvalidOrderException(item.getItemName() + " is currently unavailable");
            lines.add(new OrderLine(item.getItemId(), item.getItemName(), requested.quantity, item.getPrice()));
            total += item.getPrice() * requested.quantity;
        }
        Order order = new Order(0, student.getUserId(), student.getName(), lines, Math.round(total * 100.0) / 100.0,
                request.deliveryLocation.trim(), request.notes, OrderStatus.PENDING, Instant.now().toString());
        Order saved = repository.saveOrder(order);
        recentOrders.add(0, saved);
        return saved;
    }
    public Order updateStatus(int orderId, String rawStatus) {
        if (rawStatus == null) throw new InvalidOrderException("Status is required");
        OrderStatus status;
        try { status = OrderStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new InvalidOrderException("Status must be PENDING, PREPARING, IN_TRANSIT, or DELIVERED"); }
        if (repository.findOrder(orderId).isEmpty()) throw new InvalidOrderException("Order not found");
        repository.updateOrderStatus(orderId, status);
        return repository.findOrder(orderId).orElseThrow(() -> new InvalidOrderException("Order not found"));
    }
    public void deleteDeliveredOrder(int orderId) {
        Order order = repository.findOrder(orderId).orElseThrow(() -> new InvalidOrderException("Order not found"));
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new InvalidOrderException("Only delivered orders can be deleted");
        }
        if (!repository.deleteDeliveredOrder(orderId)) {
            throw new InvalidOrderException("Order not found or is no longer delivered");
        }
        refreshCollections();
    }
}
