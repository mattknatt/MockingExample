package com.example.payment;

public interface PaymentRepository {
    boolean save(double amount, String status);

}
