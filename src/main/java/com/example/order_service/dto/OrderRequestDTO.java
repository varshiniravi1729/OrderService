package com.example.order_service.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {

    private String productName;
    private int quantity;
    private double price;
    private String region;
}
