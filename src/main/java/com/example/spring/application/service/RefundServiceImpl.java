package com.example.spring.application.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.request.RefundRequest;
import com.example.spring.application.dto.response.RefundResponse;
import com.example.spring.domain.model.Order;
import com.example.spring.domain.model.Refund;
import com.example.spring.domain.model.RefundStatus;
import com.example.spring.domain.event.RefundCompletedEvent;
import com.example.spring.domain.event.RefundRequestedEvent;
import com.example.spring.exception.OrderException;
import com.example.spring.exception.RefundException;
import com.example.spring.domain.repository.OrderRepository;
import com.example.spring.domain.repository.RefundRepository;
import com.example.spring.application.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RefundResponse createRefund(RefundRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(request.getOrderId()));

        Refund refund = Refund.builder()
                .order(order)
                .amount(Money.of(request.getAmount()))
                .reason(request.getReason())
                .requestedBy(request.getRequestedBy())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .build();

        Refund saved = refundRepository.save(refund);

        // 환불 요청 이벤트 발행
        eventPublisher.publishEvent(new RefundRequestedEvent(saved));

        return RefundResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse findById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(id));
        return RefundResponse.from(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> findByOrderId(Long orderId) {
        return refundRepository.findByOrderId(orderId).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> findByStatus(RefundStatus status) {
        return refundRepository.findByStatus(status).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> findPendingRefunds() {
        return refundRepository.findPendingRefunds().stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponse approveRefund(Long refundId, String approver) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));

        refund.approve(approver);
        Refund saved = refundRepository.save(refund);
        return RefundResponse.from(saved);
    }

    @Override
    @Transactional
    public RefundResponse rejectRefund(Long refundId, String rejecter, String reason) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));

        refund.reject(rejecter, reason);
        Refund saved = refundRepository.save(refund);
        return RefundResponse.from(saved);
    }

    @Override
    @Transactional
    public RefundResponse startProcessing(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));

        refund.startProcessing();
        Refund saved = refundRepository.save(refund);
        return RefundResponse.from(saved);
    }

    @Override
    @Transactional
    public RefundResponse completeRefund(Long refundId, String transactionId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));

        refund.complete(transactionId);
        Refund saved = refundRepository.save(refund);

        // 환불 완료 이벤트 발행
        eventPublisher.publishEvent(new RefundCompletedEvent(saved));

        return RefundResponse.from(saved);
    }

    @Override
    @Transactional
    public RefundResponse failRefund(Long refundId, String memo) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));

        refund.fail(memo);
        Refund saved = refundRepository.save(refund);
        return RefundResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRefundedAmount(Long orderId) {
        return refundRepository.calculateTotalRefundedAmount(orderId);
    }
}