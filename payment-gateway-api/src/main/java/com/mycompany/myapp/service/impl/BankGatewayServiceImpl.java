package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.service.BankGatewayService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BankGatewayServiceImpl implements BankGatewayService {

    // Here I'm using a Map in the Hypothesis that the Bank Gateway payment processing method is indeed idempotent
    // Obviously, this state should be persisted across restarts
    final Map<String, Boolean> processedTransactions = new HashedMap<>();

    @Override
    public boolean paymentExists(PaymentDTO paymentDTO) {
        return processedTransactions.containsKey(paymentDTO.getTransactionId());
    }

    @Override
    public boolean paymentSucceeded(PaymentDTO paymentDTO) {
        return processedTransactions.get(paymentDTO.getTransactionId());
    }

    @Override
    public void createPayment(PaymentDTO paymentDTO) {
        // This method could throw an Exception if the actual BG API service returns an error code (idempotency)
        processedTransactions.put(paymentDTO.getTransactionId(), true);
    }

}
