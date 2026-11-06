package com.zut.campusbite;

import java.util.List;
import java.util.Optional;

public interface CampusBiteRepository {
    List<Student> findStudents();
    Optional<Student> findStudent(int id);
    Student saveStudent(Student student);
    List<FoodItem> findFoodItems();
    Optional<FoodItem> findFoodItem(int id);
    FoodItem saveFoodItem(FoodItem item);
    boolean deleteFoodItem(int id);
    void updateAvailability(int id, boolean available);
    Order saveOrder(Order order);
    List<Order> findOrders();
    Optional<Order> findOrder(int id);
    boolean deleteDeliveredOrder(int id);
    void updateOrderStatus(int id, OrderStatus status);
}
