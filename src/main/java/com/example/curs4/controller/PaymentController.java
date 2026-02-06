package com.example.curs4.controller;

import com.example.curs4.dto.PaymentDTO;
import com.example.curs4.entity.PaymentStatus;
import com.example.curs4.exception.CustomException;
import com.example.curs4.service.PaymentService;
import com.example.curs4.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityService securityService;

    @Operation(summary = "Создать платеж")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentDTO dto) {
        log.info("Создание платежа для клиента ID: {}", dto.getClientId());
        PaymentDTO saved = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Получить платеж по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        log.info("Получение платежа по ID: {}", id);
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Получить все платежи")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        log.info("Получение всех платежей");
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Получить платежи по клиенту")
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByClient(@PathVariable Long clientId) {
        log.info("Получение платежей для клиента ID: {}", clientId);
        List<PaymentDTO> payments = paymentService.getPaymentsByClientId(clientId);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Получить платежи по статусу")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.info("Получение платежей со статусом: {}", status);
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Получить платежи по декларации")
    @GetMapping("/declaration/{declarationId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @paymentService.isPaymentByDeclarationOwner(#declarationId, authentication))")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByDeclaration(@PathVariable Long declarationId) {
        log.info("Получение платежей для декларации ID: {}", declarationId);
        List<PaymentDTO> payments = paymentService.getPaymentsByDeclarationId(declarationId);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Обновить платеж")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @paymentService.isSinglePaymentOwner(#id, authentication))")
    public ResponseEntity<PaymentDTO> updatePayment(@PathVariable Long id,
                                                    @Valid @RequestBody PaymentDTO dto) {
        log.info("Обновление платежа ID: {}", id);
        PaymentDTO updated = paymentService.updatePayment(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Обработать платеж (оплатить)")
    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @paymentService.isSinglePaymentOwner(#id, authentication))")
    public ResponseEntity<PaymentDTO> processPayment(@PathVariable Long id) {
        log.info("Обработка платежа ID: {}", id);
        PaymentDTO processed = paymentService.processPayment(id);
        return ResponseEntity.ok(processed);
    }

    @Operation(summary = "Обновить статус платежа")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<PaymentDTO> updatePaymentStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> statusUpdate) {
        String statusStr = statusUpdate.get("status");
        PaymentStatus status = PaymentStatus.valueOf(statusStr.toUpperCase());

        log.info("Обновление статуса платежа ID: {} на: {}", id, status);
        PaymentDTO updated = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить платеж")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @paymentService.isSinglePaymentOwner(#id, authentication))")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("Удаление платежа ID: {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить статистику по клиенту")
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<Map<String, Object>> getClientStats(@PathVariable Long clientId) {
        log.info("Получение статистики платежей для клиента ID: {}", clientId);

        long total = paymentService.getPaymentsCountByClient(clientId);
        long pending = paymentService.getPaymentsCountByClientAndStatus(clientId, PaymentStatus.PENDING);
        long paid = paymentService.getPaymentsCountByClientAndStatus(clientId, PaymentStatus.PAID);

        BigDecimal totalAmount = paymentService.getTotalAmountByClient(clientId);
        BigDecimal paidAmount = paymentService.getTotalPaidAmountByClient(clientId);
        BigDecimal pendingAmount = paymentService.getTotalPendingAmountByClient(clientId);

        List<PaymentDTO> overduePayments = paymentService.getOverduePayments(clientId);
        long overdue = overduePayments.size();

        return ResponseEntity.ok(Map.of(
                "totalPayments", total,
                "pendingPayments", pending,
                "paidPayments", paid,
                "overduePayments", overdue,
                "totalAmount", totalAmount,
                "paidAmount", paidAmount,
                "pendingAmount", pendingAmount
        ));
    }

    @Operation(summary = "Получить просроченные платежи клиента")
    @GetMapping("/client/{clientId}/overdue")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<List<PaymentDTO>> getOverduePayments(@PathVariable Long clientId) {
        log.info("Получение просроченных платежей для клиента ID: {}", clientId);
        List<PaymentDTO> overduePayments = paymentService.getOverduePayments(clientId);
        return ResponseEntity.ok(overduePayments);
    }

    // Exception Handlers
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Произошла внутренняя ошибка сервера");
    }
}