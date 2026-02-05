package com.example.payment;

import com.example.NotificationException;

public interface EmailService {
    void sendPaymentConfirmation(String email, double amount) throws NotificationException;
}
