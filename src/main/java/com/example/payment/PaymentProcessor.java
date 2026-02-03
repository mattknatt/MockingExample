package com.example.payment;

import java.sql.SQLException;

public class PaymentProcessor {

    private final PaymentApi paymentApi;
    private final PaymentRepository paymentRepository;
    private static final String API_KEY = "sk_test_123456";

    public PaymentProcessor(PaymentApi paymentApi, PaymentRepository paymentRepository) {
        this.paymentApi = paymentApi;
        this.paymentRepository = paymentRepository;
    }

    public boolean processPayment(double amount) {
        // Anropar extern betaltjänst direkt med statisk API-nyckel
        PaymentApiResponse response = paymentApi.charge(API_KEY, amount);

        // Skriver till databas direkt
        if (response.isSuccess()) {
//            try {
            paymentRepository.save(amount, PaymentStatus.FAILURE.toString());
//                        .executeUpdate("INSERT INTO payments (amount, status) VALUES (" + amount + ", 'SUCCESS')");
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
        }

            // Skickar e-post direkt
            if (response.isSuccess()) {
                EmailService.sendPaymentConfirmation("user@example.com", amount);
            }

            return response.isSuccess();
        }
    }


