package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Payment getPaymentSample1() {
        return new Payment()
            .id(1L)
            .externalId("externalId1")
            .transactionId("transactionId1")
            .creditCardNumber("creditCardNumber1")
            .creditCardHolder("creditCardHolder1")
            .creditCardSecurityCode("creditCardSecurityCode1");
    }

    public static Payment getPaymentSample2() {
        return new Payment()
            .id(2L)
            .externalId("externalId2")
            .transactionId("transactionId2")
            .creditCardNumber("creditCardNumber2")
            .creditCardHolder("creditCardHolder2")
            .creditCardSecurityCode("creditCardSecurityCode2");
    }

    public static Payment getPaymentRandomSampleGenerator() {
        return new Payment()
            .id(longCount.incrementAndGet())
            .externalId(UUID.randomUUID().toString())
            .transactionId(UUID.randomUUID().toString())
            .creditCardNumber(UUID.randomUUID().toString())
            .creditCardHolder(UUID.randomUUID().toString())
            .creditCardSecurityCode(UUID.randomUUID().toString());
    }
}
