package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Order;
import com.example.spring.entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OrderRepositoryTest {
    /**
     * save_신규주문_저장성공
     * save_주문아이템포함_저장성공
     * findById_존재하는주문_주문반환
     * findById_존재하지않는주문_빈Optional반환
     * findAll_주문목록반환
     * findOrderById_존재하는주문_주문직접반환
     * findOrderById_존재하지않는주문_null반환
     */
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;


    @Test
    public void save_신규주문_저장성공() {
        //Given
        Order newOrder = Order.builder()
                .totalAmount(new BigDecimal(10000))
                .orderDate(LocalDateTime.now())
                .build();
        //When
        Order savedOrder = orderRepository.save(newOrder);

        //Then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getTotalAmount()).isEqualTo(newOrder.getTotalAmount());
        assertThat(savedOrder.getOrderDate()).isNotNull();

    }

    @Test
    @Rollback(false)
    public void save_주문아이템포함_저장성공() {
        //Given
        Book book = bookRepository.findByIdOrThrow(2L);
        Order newOrder = Order.builder()
                .totalAmount(new BigDecimal(52000))
                .orderDate(LocalDateTime.now())
                .build();
        OrderItem orderItem = OrderItem.builder()
                .book(book)
                .quantity(1)
                .price(book.getPrice())
                .build();

        newOrder.addOrderItem(orderItem);

        //When
        Order saveOrder = orderRepository.save(newOrder);

        //Then
        assertThat(saveOrder.getId()).isNotNull();
        assertThat(saveOrder.getTotalAmount()).isEqualTo(newOrder.getTotalAmount());
        assertThat(saveOrder.getOrderDate()).isNotNull();
        assertThat(saveOrder.getOrderItems()).hasSize(1);
        assertThat(saveOrder.getOrderItems().get(0).getBook()).isEqualTo(book);

        try {
            Thread.sleep(30000); // 30초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void findById_존재하는주문_주문반환() {
        //Given
        Order newOrder = Order.builder()
                .totalAmount(new BigDecimal(27000))
                .orderDate(LocalDateTime.now())
                .build();
        Order savedOrder = orderRepository.save(newOrder);

        //When
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        //Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getTotalAmount()).isEqualTo(savedOrder.getTotalAmount());
        assertThat(foundOrder.get().getOrderDate()).isEqualTo(savedOrder.getOrderDate());
        assertThat(foundOrder.get().getOrderItems()).isEmpty();
    }

    @Test
    public void findById_존재하지않는주문_빈Optional반환() {
        //When
        Optional<Order> findOrder = orderRepository.findById(150L);
        //Then
        assertThat(findOrder).isEmpty();
    }

    @Test
    public void findAll_주문목록반환() {
        //Given
        //When
        //Then
    }

    @Test
    public void findOrderById_존재하는주문_주문직접반환() {
        //Given
        //When
        //Then
    }

    @Test
    public void findOrderById_존재하지않는주문_null반환() {
        //Given
        //When
        //Then
    }
}
