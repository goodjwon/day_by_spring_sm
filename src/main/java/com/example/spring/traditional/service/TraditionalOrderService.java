package com.example.spring.traditional.service;

import com.example.spring.entity.Order;
import com.example.spring.traditional.email.TraditionalEmailService;
import com.example.spring.traditional.repository.TraditionalBookRepository;
import com.example.spring.traditional.repository.TraditionalMemberRepository;
import com.example.spring.traditional.repository.TraditionalOrderRepository;

import java.util.List;

public class TraditionalOrderService {

    private TraditionalMemberRepository memberRepository;
    private TraditionalOrderRepository orderRepository;
    private TraditionalBookRepository bookRepository;
    private TraditionalEmailService emailService;

    public TraditionalOrderService() {
        this.memberRepository = new TraditionalMemberRepository();
        this.orderRepository = new TraditionalOrderRepository();
        this.bookRepository = new TraditionalBookRepository();
        this.emailService = new TraditionalEmailService();
    }


    // 기능정의 하기.
    Order createOrder(List<Long> bookIds){
        
        return null;
    }

    //findOrderById
    public Order findOrderById(Long id){
        return null;
    }

    //findAllOrders
    public List<Order> findAllOrders(){
        return null;
    }
}
