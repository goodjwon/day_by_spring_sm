package com.example.spring.service;

import com.example.spring.dto.request.RefundRequest;
import com.example.spring.dto.response.RefundResponse;
import com.example.spring.entity.RefundStatus;

import java.math.BigDecimal;
import java.util.List;

public interface RefundService {
    RefundResponse createRefund(RefundRequest refundRequest);
    RefundResponse findById(Long refundId);
    List<RefundResponse> findByOrderId(Long orderId);
    List<RefundResponse> findByStatus(RefundStatus status);
    List<RefundResponse> findPendingRefunds();
    RefundResponse approveRefund(Long refundId, String approvedBy);
    RefundResponse rejectRefund(Long refundId, String rejectedBy,  String reason);
    RefundResponse startProcessing(Long refundId);
    RefundResponse completeRefund(Long refundId, String transactionId);
    RefundResponse failRefund(Long refundId, String processingMemo);
    BigDecimal getTotalRefundedAmount(Long orderId);
}
