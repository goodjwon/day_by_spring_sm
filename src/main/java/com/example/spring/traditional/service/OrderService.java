package com.example.spring.traditional.service;

import com.example.spring.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(List<Long> bookIds);
    Order findOrderById(Long id);
    List<Order> findAllOrders();
}