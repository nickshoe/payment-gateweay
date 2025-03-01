package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.PaymentDTO;

public interface BankGatewayService {
    boolean paymentExists(PaymentDTO paymentDTO);

    void createPayment(PaymentDTO paymentDTO);
}
