package com.example.payment;

import com.example.NotificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Processor Tests")
class PaymentProcessorTest {

    @Mock
    private PaymentApi paymentApi;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentCredentials paymentCredentials;
    @Mock
    private EmailService emailService;

    @InjectMocks
    PaymentProcessor paymentProcessor;


    /**
     * Verifies that an IllegalArgumentException is thrown if the payment amount is negative.
     */
    @Test
    @DisplayName("Process payment: Negative amount should throw IllegalArgumentException")
    void shouldThrowException_ifAmountIsNegative() {
        double amount = -0.5;

        assertThatThrownBy(() ->paymentProcessor.processPayment(amount))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must be positive");
    }

    /**
     * Verifies that an IllegalArgumentException is thrown if the payment amount is zero.
     */
    @Test
    @DisplayName("Process payment: Zero amount should throw IllegalArgumentException")
    void shouldThrowException_ifAmountIsZero() {
        double amount = 0.0;

        assertThatThrownBy(() ->paymentProcessor.processPayment(amount))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must be positive");
    }

    /**
     * Verifies that the payment is recorded as a failure and returns false when the Payment API throws an exception.
     */
    @Test
    @DisplayName("Should record failure when API throws exception")
    void shouldSavePaymentWithFailureMessage_whenResponseIsFalse() {
        double amount = 1.2;
        when(paymentApi.charge(anyString(), eq(amount))).thenThrow(new RuntimeException());

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isFalse();
        verify(paymentRepository).save(amount, PaymentStatus.FAILURE.toString());
    }

    /**
     * Verifies that the payment is recorded as successful and returns true when the API response indicates success.
     */
    @Test
    @DisplayName("Should record success when API returns success")
    void shouldSavePaymentWithSuccessMessage_whenResponseIsSuccess() {
        double amount = 1.2;
        var response = new PaymentApiResponse(true);

        when(paymentApi.charge(any(), eq(amount))).thenReturn(response);

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isTrue();
        verify(paymentRepository).save(eq(amount), eq(PaymentStatus.SUCCESS.toString()));

    }

    /**
     * Verifies that the payment is recorded as a failure and returns false when the API returns a failure response.
     */
    @Test
    @DisplayName("Should return false when API-response fails")
    void shouldReturnFalse_whenResponseIsFalse() {
        double amount = 1.2;
        var response = new PaymentApiResponse(false);

        when(paymentApi.charge(any(), eq(amount))).thenReturn(response);

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isFalse();
        verify(paymentRepository).save(eq(amount), eq(PaymentStatus.FAILURE.toString()));
    }

    /**
     * Verifies that the payment remains successful even if the email confirmation fails.
     */
    @Test
    @DisplayName("Should still return true and save payment if email notification fails")
    void shouldSavePayment_whenEmailFails() throws NotificationException {
        double amount = 1.2;
        var response = new PaymentApiResponse(true);
        when(paymentApi.charge(any(), eq(amount))).thenReturn(response);
        doThrow(new NotificationException("Email failed")).when(emailService).sendPaymentConfirmation(any(), anyDouble());

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isTrue();
        verify(paymentRepository).save(amount, PaymentStatus.SUCCESS.toString());
    }
}