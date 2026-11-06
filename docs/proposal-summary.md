# CampusBite Delivery Hub — Proposal Summary

## Problem
Students with tight lecture schedules or late-night study sessions may not have enough time to visit campus canteens or nearby food outlets. Vendors may rely on calls or paper lists, leading to miscommunication, lost orders, queues, and poor visibility of order progress.

## Proposed solution
CampusBite centralises meal browsing, order placement, order validation, vendor status updates, and basic order history in one application.

## Target users
- **Students:** browse available meals, choose quantities, enter delivery details, and check status.
- **Campus vendors/caterers:** maintain menu items, review orders, and update preparation/delivery statuses.

## Main classes
`User`, `Student`, `FoodItem`, `Order`, `DeliveryManager`, `InvalidOrderException`.

## OOP concepts demonstrated
- Encapsulation: private fields and public methods.
- Inheritance: `Student` extends abstract `User`.
- Abstraction: `User` defines shared user data/behaviour.
- Collections: lists and maps manage menu items, students, and orders in the service layer.
- Custom exceptions: invalid order requests produce `InvalidOrderException`.

## Technologies
Java 17, Javalin, Java Collections, SQLite, HTML, CSS, JavaScript.

## Scope limitations
This prototype records orders and statuses. It does not implement online payment, GPS navigation, real-time courier location, or SMS/email notifications.
