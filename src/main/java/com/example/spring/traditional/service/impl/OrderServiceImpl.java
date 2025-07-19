package com.example.spring.traditional.service.impl;

import com.example.spring.entity.Order;
import com.example.spring.traditional.service.OrderService;

import java.util.List;

public class OrderServiceImpl implements OrderService {

    @Override
    public Order createOrder(List<Long> bookIds) {
        return null;
    }

    @Override
    public Order findOrderById(Long id) {
        return null;
    }

    @Override
    public List<Order> findAllOrders() {
        return List.of();
    }
}
