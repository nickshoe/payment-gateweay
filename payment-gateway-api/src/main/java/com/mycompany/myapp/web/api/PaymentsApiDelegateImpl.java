package com.mycompany.myapp.web.api;

import com.mycompany.myapp.service.PaymentRequestService;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.api.dto.PaymentRequest;
import com.mycompany.myapp.service.api.dto.QueuePaymentRequestCommand;
import com.mycompany.myapp.service.dto.PaymentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentsApiDelegateImpl implements PaymentsRequestsApiDelegate {

    private final PaymentRequestService paymentRequestService;
    private final PaymentService paymentService;

    public PaymentsApiDelegateImpl(PaymentRequestService paymentRequestService, PaymentService paymentService) {
        this.paymentRequestService = paymentRequestService;
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<Void> queuePaymentRequest(String paymentId, QueuePaymentRequestCommand queuePaymentRequestCommand) {
        String transactionId = queuePaymentRequestCommand.getTransactionId();
        BigDecimal amount = new BigDecimal(queuePaymentRequestCommand.getAmount());
        String creditCardNumber = queuePaymentRequestCommand.getCreditCardInfo().getNumber();
        String creditCardHolder = queuePaymentRequestCommand.getCreditCardInfo().getHolder();
        String creditCardSecurityCode = queuePaymentRequestCommand.getCreditCardInfo().getSecurityCode();

        paymentRequestService.queuePaymentRequest(
            paymentId,
            transactionId,
            creditCardNumber,
            creditCardHolder,
            creditCardSecurityCode,
            amount
        );

        // TODO: build URL from configs
        String resourceUrl = "http://localhost:8080/api/payments/" + paymentId;

        return ResponseEntity
            .noContent()
            .header("Location", resourceUrl)
            .build();
    }

    @Override
    public ResponseEntity<PaymentRequest> readPaymentRequest(String paymentId) {
        Optional<PaymentDTO> paymentDTOOptional = paymentService.findOneByExternalId(paymentId);

        if (paymentDTOOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PaymentDTO paymentDTO = paymentDTOOptional.orElseThrow();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setId(paymentId);
        paymentRequest.setCreatedAt(paymentDTO.getCreatedAt().toString());
        if (paymentDTO.getUpdatedAt() != null) {
            paymentRequest.setUpdatedAt(paymentDTO.getUpdatedAt().toString());
        }
        paymentRequest.setStatus(PaymentRequest.StatusEnum.fromValue(paymentDTO.getStatus().toString()));
        paymentRequest.setResult(PaymentRequest.ResultEnum.fromValue(paymentDTO.getResult().toString()));

        return ResponseEntity.ok(paymentRequest);
    }
}
