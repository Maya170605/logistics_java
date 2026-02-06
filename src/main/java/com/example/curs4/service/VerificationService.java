package com.example.curs4.service;

import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    public boolean verifyUNP(String unp) {
        if (unp == null) {
            return false;
        }
        // Проверка формата УНП — 9 цифр
        if (!unp.matches("^\\d{9}$")) {
            return false;
        }
        return true;
    }
}


