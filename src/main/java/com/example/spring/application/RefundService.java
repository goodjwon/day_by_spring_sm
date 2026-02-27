package com.example.spring.application;

import com.example.spring.application.dto.request.RefundRequest;
import com.example.spring.application.dto.response.RefundResponse;
import com.example.spring.domain.model.RefundStatus;

import java.math.BigDecimal;
import java.util.List;

public interface RefundService {
    // 환불 생성 및 조회
    RefundResponse createRefund(RefundRequest request);
    RefundResponse findById(Long id);
    List<RefundResponse> findByOrderId(Long orderId);
    List<RefundResponse> findByStatus(RefundStatus status);
    List<RefundResponse> findPendingRefunds();

    // 환불 처리
    RefundResponse approveRefund(Long refundId, String approver);
    RefundResponse rejectRefund(Long refundId, String rejecter, String reason);
    RefundResponse startProcessing(Long refundId);
    RefundResponse completeRefund(Long refundId, String transactionId);
    RefundResponse failRefund(Long refundId, String memo);

    // 환불 통계
    BigDecimal getTotalRefundedAmount(Long orderId);
}