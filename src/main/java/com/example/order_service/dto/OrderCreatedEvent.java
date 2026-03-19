package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private double amount;
    ///cloned mam's repo
    public void msg(){
        String str = "hi hello what do u doo";
    }

}
