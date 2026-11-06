package com.zut.campusbite;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String dbPath = System.getProperty("campusbite.db", "campusbite.db");
        DeliveryManager manager = new DeliveryManager(new SqliteCampusBiteRepository(dbPath));
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static");
            config.http.defaultContentType = "application/json";
        });
        app.exception(InvalidOrderException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage() == null ? "Invalid request" : e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected server error", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Unexpected server error"));
        });

        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok", "application", "CampusBite Delivery Hub")));
        app.get("/api/menu", ctx -> ctx.json(manager.getMenu()));
        app.post("/api/menu", ctx -> {
            FoodItemRequest body = ctx.bodyAsClass(FoodItemRequest.class);
            if (body.isAvailable == null) body.isAvailable = true;
            ctx.status(HttpStatus.CREATED).json(manager.addFoodItem(body.itemName, body.price, body.isAvailable, body.imageData));
        });
        app.delete("/api/menu/{id}", ctx -> {
            manager.deleteFoodItem(Integer.parseInt(ctx.pathParam("id")));
            ctx.json(Map.of("message", "Food item deleted"));
        });
        app.patch("/api/menu/{id}/availability", ctx -> {
            Map<?,?> body = ctx.bodyAsClass(Map.class);
            Object available = body.get("isAvailable");
            if (!(available instanceof Boolean value)) throw new InvalidOrderException("isAvailable must be true or false");
            manager.setAvailability(Integer.parseInt(ctx.pathParam("id")), value);
            ctx.json(Map.of("message", "Availability updated"));
        });
        app.get("/api/students", ctx -> ctx.json(manager.getStudents()));
        app.post("/api/students", ctx -> {
            StudentRequest b = ctx.bodyAsClass(StudentRequest.class);
            ctx.status(HttpStatus.CREATED).json(manager.registerStudent(b.name,b.phone,b.email,b.hostelName,b.roomNumber));
        });
        app.get("/api/orders", ctx -> ctx.json(manager.getOrders()));
        app.post("/api/orders", ctx -> ctx.status(HttpStatus.CREATED).json(manager.placeOrder(ctx.bodyAsClass(OrderRequest.class))));
        app.delete("/api/orders/{id}", ctx -> {
            manager.deleteDeliveredOrder(Integer.parseInt(ctx.pathParam("id")));
            ctx.json(Map.of("message", "Delivered order deleted"));
        });
        app.patch("/api/orders/{id}/status", ctx -> {
            StatusRequest b = ctx.bodyAsClass(StatusRequest.class);
            ctx.json(manager.updateStatus(Integer.parseInt(ctx.pathParam("id")), b.status));
        });
        app.start(7070);
        System.out.println("CampusBite is running at http://localhost:7070");
    }
}
