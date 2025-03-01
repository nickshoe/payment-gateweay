package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.service.BankGatewayService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class BankGatewayServiceImpl implements BankGatewayService {

    // Here I'm using a Set in the Hypothesis that the Bank Gateway payment processing method is indeed idempotent
    final Set<String> processedTransactionsIds = new HashSet<>();

    @Override
    public boolean paymentExists(PaymentDTO paymentDTO) {
        return processedTransactionsIds.contains(paymentDTO.getTransactionId());
    }

    @Override
    public void createPayment(PaymentDTO paymentDTO) {
        // This method could throw an Exception if the actual BG API service returns an error code (idempotency)
        processedTransactionsIds.add(paymentDTO.getTransactionId());
    }

}
