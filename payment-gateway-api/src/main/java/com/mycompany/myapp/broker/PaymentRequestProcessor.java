package com.mycompany.myapp.broker;

import com.mycompany.myapp.domain.enumeration.PaymentResult;
import com.mycompany.myapp.domain.enumeration.PaymentStatus;
import com.mycompany.myapp.service.BankGatewayService;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Consumer;

@Component
public class PaymentRequestProcessor implements Consumer<Message<PaymentDTO>> {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentRequestProcessor.class);

    private final PaymentService paymentService;
    private final BankGatewayService bankGatewayService;

    public PaymentRequestProcessor(PaymentService paymentService, BankGatewayService bankGatewayService) {
        this.paymentService = paymentService;
        this.bankGatewayService = bankGatewayService;
    }

    @Override
    public void accept(Message<PaymentDTO> message) {
        PaymentDTO paymentDTO = message.getPayload();

        LOG.debug("Got payment request from queue: {}", paymentDTO);

        LOG.debug("Starting to process the payment request: {}", paymentDTO);
        paymentDTO.setStatus(PaymentStatus.RUNNING);

        // the upstream Bank Gateway service createPayment operation is not idempotent
        // multiple calls to the `createPayment` method would lead to multiple payments being processed

        // hypothesis: Bank Gateway service provide a method to check if a payment had already been processed
        if (bankGatewayService.paymentExists(paymentDTO)) {
            handleAlreadyProcessedPaymentRequest(paymentDTO);
        } else {
            processPaymentRequest(paymentDTO);
        }

        paymentDTO.setStatus(PaymentStatus.TERMINATED);
        paymentDTO.setUpdatedAt(Instant.now());
        paymentService.save(paymentDTO);

        ackMessage(message);
    }

    private void processPaymentRequest(PaymentDTO paymentDTO) {
        try {
            bankGatewayService.createPayment(paymentDTO);

            LOG.error("Payment request processing succeeded: {}", paymentDTO);

            paymentDTO.setResult(PaymentResult.SUCCESS);
        } catch (Exception e) {
            LOG.error("Payment request processing has failed: {}", paymentDTO);

            paymentDTO.setResult(PaymentResult.FAILURE);
        }
    }

    private void handleAlreadyProcessedPaymentRequest(PaymentDTO paymentDTO) {
        if (bankGatewayService.paymentSucceeded(paymentDTO)) {
            LOG.error("Payment request was already processed and succeeded: {}", paymentDTO);

            paymentDTO.setResult(PaymentResult.SUCCESS);
        } else {
            LOG.error("Payment request was already processed and failed: {}", paymentDTO);

            paymentDTO.setResult(PaymentResult.FAILURE);
        }
    }

    private static void ackMessage(Message<?> message) {
        Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
        if (acknowledgment != null) {
            System.out.println("Acknowledgment provided");
            acknowledgment.acknowledge();
        }
    }
}
