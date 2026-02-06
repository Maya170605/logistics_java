package com.example.curs4.mapper;

import com.example.curs4.dto.PaymentDTO;
import com.example.curs4.entity.Payment;
import com.example.curs4.entity.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        String clientName = null;
        if (payment.getClient() != null) {
            clientName = payment.getClient().getName();
            if (clientName == null) {
                clientName = payment.getClient().getUsername();
            }
        }

        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .clientId(payment.getClient() != null ? payment.getClient().getId() : null)
                .clientName(clientName)
                .declarationId(payment.getDeclaration() != null ? payment.getDeclaration().getId() : null)
                .declarationNumber(payment.getDeclaration() != null ? payment.getDeclaration().getDeclarationNumber() : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .dueDate(payment.getDueDate())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
               // .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public Payment toEntity(PaymentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Payment.builder()
                .id(dto.getId())
                .paymentNumber(dto.getPaymentNumber())
                .amount(dto.getAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "EUR")
                .paymentType(dto.getPaymentType())
                .status(dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PENDING)
                .dueDate(dto.getDueDate())
                .paidAt(dto.getPaidAt())
                .build();
    }
}