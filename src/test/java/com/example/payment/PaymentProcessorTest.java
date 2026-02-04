package com.example.payment;

import com.example.NotificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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


    @Test
    void shouldThrowException_ifAmountIsNotPositive() {
        double amount = -0.5;

        assertThatThrownBy(() ->paymentProcessor.processPayment(amount))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must be positive");
    }

    @Test
    void shouldSavePaymentWithFailureMessage_whenResponseIsFalse() {
        double amount = 1.2;
        when(paymentApi.charge(anyString(), eq(amount))).thenThrow(new RuntimeException());

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isFalse();
        verify(paymentRepository).save(amount, PaymentStatus.FAILURE.toString());
    }

    @Test
    void shouldSavePaymentWithSuccessMessage_whenResponseIsSuccess() {
        double amount = 1.2;
        var response = mock(PaymentApiResponse.class);
        when(response.isSuccess()).thenReturn(true);

        when(paymentApi.charge(anyString(), eq(amount))).thenReturn(response);

        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isTrue();
        verify(paymentRepository).save(eq(amount), eq(PaymentStatus.SUCCESS.toString()));

    }

}