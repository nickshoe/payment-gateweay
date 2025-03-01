package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AssertUtils.bigDecimalCompareTo;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertPaymentAllPropertiesEquals(Payment expected, Payment actual) {
        assertPaymentAutoGeneratedPropertiesEquals(expected, actual);
        assertPaymentAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertPaymentAllUpdatablePropertiesEquals(Payment expected, Payment actual) {
        assertPaymentUpdatableFieldsEquals(expected, actual);
        assertPaymentUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertPaymentAutoGeneratedPropertiesEquals(Payment expected, Payment actual) {
        assertThat(actual)
            .as("Verify Payment auto generated properties")
            .satisfies(a -> assertThat(a.getId()).as("check id").isEqualTo(expected.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertPaymentUpdatableFieldsEquals(Payment expected, Payment actual) {
        assertThat(actual)
            .as("Verify Payment relevant properties")
            .satisfies(a -> assertThat(a.getExternalId()).as("check externalId").isEqualTo(expected.getExternalId()))
            .satisfies(a -> assertThat(a.getTransactionId()).as("check transactionId").isEqualTo(expected.getTransactionId()))
            .satisfies(a -> assertThat(a.getCreditCardNumber()).as("check creditCardNumber").isEqualTo(expected.getCreditCardNumber()))
            .satisfies(a -> assertThat(a.getCreditCardHolder()).as("check creditCardHolder").isEqualTo(expected.getCreditCardHolder()))
            .satisfies(a ->
                assertThat(a.getCreditCardSecurityCode()).as("check creditCardSecurityCode").isEqualTo(expected.getCreditCardSecurityCode())
            )
            .satisfies(a ->
                assertThat(a.getAmount()).as("check amount").usingComparator(bigDecimalCompareTo).isEqualTo(expected.getAmount())
            )
            .satisfies(a -> assertThat(a.getCreatedAt()).as("check createdAt").isEqualTo(expected.getCreatedAt()))
            .satisfies(a -> assertThat(a.getUpdatedAt()).as("check updatedAt").isEqualTo(expected.getUpdatedAt()))
            .satisfies(a -> assertThat(a.getStatus()).as("check status").isEqualTo(expected.getStatus()))
            .satisfies(a -> assertThat(a.getResult()).as("check result").isEqualTo(expected.getResult()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertPaymentUpdatableRelationshipsEquals(Payment expected, Payment actual) {
        // empty method
    }
}
