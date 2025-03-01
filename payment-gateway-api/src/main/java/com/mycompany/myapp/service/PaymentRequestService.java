package com.mycompany.myapp.service;

import java.math.BigDecimal;

public interface PaymentRequestService {
    void queuePaymentRequest(
        String paymentExternalId,
        String transactionId,
        String creditCardNumber,
        String creditCardHolder,
        String creditCardSecurityCode,
        BigDecimal amount
    );
}
