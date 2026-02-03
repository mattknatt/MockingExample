package com.example.payment;

import java.sql.PreparedStatement;

public interface DatabaseConnection {
    static PreparedStatement getInstance() {
    }
}
