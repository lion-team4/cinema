package com.example.cinema.type;

public enum PaymentStatus {
    APPROVED,
    FAILED,
    CANCELED,
    REFUNDED;

    public static PaymentStatus fromTossStatus(String status){
        return PaymentStatus.valueOf(status.toUpperCase());
    }

    public static PaymentStatus tossPaymentStatus(String status){
        if (status.equals("DONE")) return  PaymentStatus.APPROVED;
        return PaymentStatus.FAILED;
    }
}
