package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.enumeration.PaymentStatus;
import com.mycompany.myapp.service.PaymentRequestService;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentRequestServiceImpl.class);

    private final PaymentService paymentService;
    private final StreamBridge streamBridge;

    public PaymentRequestServiceImpl(PaymentService paymentService, StreamBridge streamBridge) {
        this.paymentService = paymentService;
        this.streamBridge = streamBridge;
    }

    @Override
    public void queuePaymentRequest(
        String paymentExternalId,
        String transactionId,
        String creditCardNumber,
        String creditCardHolder,
        String creditCardSecurityCode,
        BigDecimal amount
    ) {
        Optional<PaymentDTO> existingPaymentRequest = paymentService.findOneByExternalId(paymentExternalId);

        if (existingPaymentRequest.isPresent()) {
            LOG.debug("Payment request already exists with external id {}", paymentExternalId);

            return;
        }

        LOG.debug("No existing payment request found with external id {}", paymentExternalId);

        PaymentDTO paymentDTO = storePayment(
            paymentExternalId,
            transactionId,
            creditCardNumber,
            creditCardHolder,
            creditCardSecurityCode,
            amount
        );

        streamBridge.send(
            "paymentRequests",
            paymentDTO
        );

        paymentDTO.setStatus(PaymentStatus.READY);
        paymentDTO = paymentService.save(paymentDTO);
    }

    private PaymentDTO storePayment(String paymentExternalId, String transactionId, String creditCardNumber, String creditCardHolder, String creditCardSecurityCode, BigDecimal amount) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setExternalId(paymentExternalId);
        paymentDTO.setTransactionId(transactionId);
        paymentDTO.setCreditCardNumber(creditCardNumber);
        paymentDTO.setCreditCardHolder(creditCardHolder);
        paymentDTO.setCreditCardSecurityCode(creditCardSecurityCode);
        paymentDTO.setAmount(amount);
        paymentDTO.setCreatedAt(Instant.now());
        paymentDTO.setStatus(PaymentStatus.NEW);

        paymentDTO = paymentService.save(paymentDTO);

        return paymentDTO;
    }

}
