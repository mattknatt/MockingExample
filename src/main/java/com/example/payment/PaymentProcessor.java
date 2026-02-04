package com.example.payment;

import com.example.NotificationException;

public class PaymentProcessor {

    private final PaymentApi paymentApi;
    private final PaymentRepository paymentRepository;
    private final PaymentCredentials paymentCredentials;
    private final EmailService emailService;


    public PaymentProcessor(PaymentApi paymentApi, PaymentRepository paymentRepository, PaymentCredentials paymentCredentials, EmailService emailService) {
        this.paymentApi = paymentApi;
        this.paymentRepository = paymentRepository;
        this.paymentCredentials = paymentCredentials;
        this.emailService = emailService;
    }
    public boolean processPayment(double amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        PaymentApiResponse response;
        try {
            response = paymentApi.charge(paymentCredentials.getApiKey(), amount);
        } catch (RuntimeException e) {
            paymentRepository.save(amount, PaymentStatus.FAILURE.toString());
            return false;
        }

        if (response.isSuccess()) {
            paymentRepository.save(amount, PaymentStatus.SUCCESS.toString());

            try {
                emailService.sendPaymentConfirmation(paymentCredentials.getEmailAddress(), amount);
            } catch (NotificationException e) {
            }

            return true;
        }
        paymentRepository.save(amount, PaymentStatus.FAILURE.toString());
        return false;
    }
}


