package com.zut.campusbite;

import java.util.List;

public class OrderRequest {
    public int studentId;
    public List<ItemRequest> items;
    public String deliveryLocation;
    public String notes;

    public static class ItemRequest {
        public int foodItemId;
        public int quantity;
    }
}
