package com.example.spring.service.impl;

import com.example.spring.domain.vo.Money;
import com.example.spring.dto.request.RefundRequest;
import com.example.spring.dto.response.RefundResponse;
import com.example.spring.entity.Order;
import com.example.spring.entity.Refund;
import com.example.spring.entity.RefundStatus;
import com.example.spring.exception.OrderException;
import com.example.spring.exception.RefundException;
import com.example.spring.repository.OrderRepository;
import com.example.spring.repository.RefundRepository;
import com.example.spring.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundServiceImpl implements RefundService {
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;

    @Override
    public RefundResponse createRefund(RefundRequest refundRequest) {
        log.info("환불 요청 생성 요청 - ID: {}", refundRequest.getRefundId());
        Order order = orderRepository.findById(refundRequest.getOrderId())
                .orElseThrow(() -> new OrderException.OrderNotFoundException(refundRequest.getOrderId()));
        Refund refund = Refund.builder()
                .id(refundRequest.getRefundId())
                .order(order)
                .amount(Money.of(refundRequest.getAmount()))
                .reason(refundRequest.getReason())
                .status(RefundStatus.PENDING)
                .build();
        Refund savedRefund = refundRepository.save(refund);
        savedRefund.setStatus(RefundStatus.REQUESTED);
        refundRepository.save(savedRefund);
        log.info("환불 요청 생성 완료 - ID: {}", refundRequest.getRefundId());
        return RefundResponse.from(savedRefund);
    }

    @Override
    public RefundResponse findById(Long refundId) {
        log.info("환불 ID로 조회 요청 - ID: {}", refundId);
        log.info("환불 ID로 조회 완료 - ID: {}", refundId);
        return RefundResponse.from(refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId)));
    }

    @Override
    public List<RefundResponse> findByOrderId(Long orderId) {
        log.info("주문 ID로 환불 목록 조회 요청 -  ID: {}", orderId);
        List<Refund> refundList = refundRepository.findByOrderId(orderId);
        log.info("주문 ID로 환불 목록 조회 완료 -  ID: {}", orderId);
        return refundList.stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundResponse> findByStatus(RefundStatus status) {
        log.info("상태별 환불 목록 조회 요청 -  상태: {}", status);
        List<Refund> refundList = refundRepository.findByStatus(status);
        log.info("상태별 환불 목록 조회 완료 -  상태: {}", status);
        return refundList.stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundResponse> findPendingRefunds() {
        log.info("대기 중인 환불 목록 조회 요청");
        List<Refund> refundList = refundRepository.findPendingRefunds();
        log.info("대기 중인 환불 목록 조회 완료");
        return refundList.stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public RefundResponse approveRefund(Long refundId, String approvedBy) {
        log.info("환불 승인 요청 - ID: {}, 승인자: {}", refundId,  approvedBy);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));
        if (RefundStatus.APPROVED.equals(refund.getStatus())) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 승인");
        }
        refund.setStatus(RefundStatus.APPROVED);
        refundRepository.save(refund);
        log.info("환불 승인 완료 - ID: {}", refundId);
        return RefundResponse.from(refund);
    }

    @Override
    public RefundResponse rejectRefund(Long refundId, String rejectedBy, String reason) {
        log.info("환불 거부 요청 - ID: {}, 거부자: {}, 사유: {}", refundId, rejectedBy,  reason);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));
        if (RefundStatus.APPROVED.equals(refund.getStatus())) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 거부");
        }
        refund.setStatus(RefundStatus.REJECTED);
        refundRepository.save(refund);
        log.info("환불 거부 완료 - ID: {}", refundId);
        return RefundResponse.from(refund);
    }

    @Override
    public RefundResponse startProcessing(Long refundId) {
        log.info("환불 처리 시작 요청 - ID: {}", refundId);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));
        if (!RefundStatus.APPROVED.equals(refund.getStatus())) {
            throw new RefundException.InvalidRefundStateException("승인된 환불만 처리를 시작");
        }
        refund.setStatus(RefundStatus.PROCESSING);
        refundRepository.save(refund);
        log.info("환불 처리 시작 완료 - ID: {}", refundId);
        return RefundResponse.from(refund);
    }

    @Override
    public RefundResponse completeRefund(Long refundId, String transactionId) {
        log.info("환불 완료 요청 - ID: {}, 거래 ID: {}", refundId, transactionId);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));
        if (!RefundStatus.PROCESSING.equals(refund.getStatus())) {
            throw new RefundException.InvalidRefundStateException("처리중인 환불만 완료");
        }
        refund.setStatus(RefundStatus.COMPLETED);
        refundRepository.save(refund);
        log.info("환불 완료 - ID: {}", refundId);
        return RefundResponse.from(refund);
    }

    @Override
    public RefundResponse failRefund(Long refundId, String processingMemo) {
        log.info("환불 실패 처리 요청 - ID: {}, 처리 사유: {}", refundId, processingMemo);
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundException.RefundNotFoundException(refundId));
        refund.setStatus(RefundStatus.FAILED);
        refundRepository.save(refund);
        log.info("환불 실패 처리 완료 - ID: {}", refundId);
        return RefundResponse.from(refund);
    }

    @Override
    public BigDecimal getTotalRefundedAmount(Long orderId) {
        log.info("총 환불 금액 조회 요청 - ID: {}", orderId);
        BigDecimal amount = refundRepository.calculateTotalRefundedAmount(orderId);
        log.info("총 환불 금액 조회 완료 - ID: {}", orderId);
        return amount;
    }
}
