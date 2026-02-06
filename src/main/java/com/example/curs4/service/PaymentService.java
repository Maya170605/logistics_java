package com.example.curs4.service;

import com.example.curs4.dto.PaymentDTO;
import com.example.curs4.entity.Declaration;
import com.example.curs4.entity.Payment;
import com.example.curs4.entity.PaymentStatus;
import com.example.curs4.entity.User;
import com.example.curs4.exception.CustomException;
import com.example.curs4.mapper.PaymentMapper;
import com.example.curs4.repository.DeclarationRepository;
import com.example.curs4.repository.PaymentRepository;
import com.example.curs4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DeclarationRepository declarationRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityService securityService;

    // CREATE
    public PaymentDTO createPayment(PaymentDTO dto) {
        log.info("Создание платежа для клиента ID: {}", dto.getClientId());

        validatePayment(dto);

        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new CustomException("Клиент не найден"));

        Payment payment = paymentMapper.toEntity(dto);
        payment.setClient(client);
        payment.setPaymentNumber(generatePaymentNumber());

        // Привязка декларации если указана
        if (dto.getDeclarationId() != null) {
            Declaration declaration = declarationRepository.findById(dto.getDeclarationId())
                    .orElseThrow(() -> new CustomException("Декларация не найдена"));
            payment.setDeclaration(declaration);
        }

        // Устанавливаем значения по умолчанию
        if (payment.getAmount() == null) {
            payment.setAmount(BigDecimal.ZERO);
        }
        if (payment.getDueDate() == null) {
            payment.setDueDate(LocalDate.now().plusDays(14));
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Платеж создан: {}", savedPayment.getPaymentNumber());

        return paymentMapper.toDto(savedPayment);
    }

    // READ
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Платеж не найден"));
        return paymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByClientId(Long clientId) {
        return paymentRepository.findByClientId(clientId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByDeclarationId(Long declarationId) {
        return paymentRepository.findByDeclarationId(declarationId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    // UPDATE
    public PaymentDTO updatePayment(Long id, PaymentDTO dto) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Платеж не найден"));

        // Проверяем, можно ли редактировать (только PENDING)
        if (existingPayment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException("Редактирование невозможно. Платеж уже обработан.");
        }

        // Обновляем разрешенные поля
        existingPayment.setAmount(dto.getAmount());
        existingPayment.setPaymentType(dto.getPaymentType());
        existingPayment.setCurrency(dto.getCurrency());
        existingPayment.setDueDate(dto.getDueDate());

        // Обновляем привязку к декларации
        if (dto.getDeclarationId() != null) {
            Declaration declaration = declarationRepository.findById(dto.getDeclarationId())
                    .orElseThrow(() -> new CustomException("Декларация не найдена"));
            existingPayment.setDeclaration(declaration);
        } else {
            existingPayment.setDeclaration(null);
        }

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Платеж обновлен: {}", updatedPayment.getPaymentNumber());

        return paymentMapper.toDto(updatedPayment);
    }

    // PROCESS PAYMENT
    public PaymentDTO processPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Платеж не найден"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException("Платеж уже обработан");
        }

        // Имитация успешной оплаты
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        Payment processedPayment = paymentRepository.save(payment);
        log.info("Платеж обработан: {}", processedPayment.getPaymentNumber());

        return paymentMapper.toDto(processedPayment);
    }

    // UPDATE STATUS
    public PaymentDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Платеж не найден"));

        payment.setStatus(status);

        if (status == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Статус платежа {} изменен на: {}",
                updatedPayment.getPaymentNumber(), status);

        return paymentMapper.toDto(updatedPayment);
    }

    // DELETE
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Платеж не найден"));

        // Проверяем, можно ли удалить (только PENDING для клиентов, админ может удалять любые)
        boolean isAdmin = securityService.isAdmin();
        if (!isAdmin && payment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException("Удаление невозможно. Платеж уже обработан.");
        }

        paymentRepository.delete(payment);
        log.info("Платеж удален: {}", payment.getPaymentNumber());
    }

    // VALIDATION
    private void validatePayment(PaymentDTO dto) {
        if (dto.getClientId() == null) {
            throw new CustomException("ID клиента обязателен");
        }

        if (dto.getAmount() == null) {
            throw new CustomException("Сумма платежа обязательна");
        }
        
        if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Сумма платежа не может быть отрицательной");
        }
    }

    // STATISTICS
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        BigDecimal amount = paymentRepository.getTotalAmountByClient(client);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        BigDecimal amount = paymentRepository.getTotalPaidAmountByClient(client);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        BigDecimal amount = paymentRepository.getTotalPendingAmountByClient(client);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getOverduePayments(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return paymentRepository.findOverduePayments(client, LocalDate.now()).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    // UTILITY METHODS
    private String generatePaymentNumber() {
        String baseNumber = "PMT-" + LocalDateTime.now().getYear() + "-";
        String paymentNumber;
        int attempt = 0;
        
        do {
            long count = paymentRepository.count() + 1 + attempt;
            paymentNumber = baseNumber + String.format("%05d", count);
            attempt++;
            
            // Защита от бесконечного цикла
            if (attempt > 1000) {
                paymentNumber = baseNumber + String.format("%05d", System.currentTimeMillis() % 100000);
                break;
            }
        } while (paymentRepository.existsByPaymentNumber(paymentNumber));
        
        return paymentNumber;
    }

    @Transactional(readOnly = true)
    public boolean paymentExists(Long id) {
        return paymentRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long getPaymentsCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return paymentRepository.countByClient(client);
    }

    @Transactional(readOnly = true)
    public long getPaymentsCountByClientAndStatus(Long clientId, PaymentStatus status) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return paymentRepository.countByClientAndStatus(client, status);
    }
    public boolean isPaymentOwner(Long clientId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return currentUser.getId().equals(clientId);
    }

    public boolean isSinglePaymentOwner(Long paymentId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException("Платеж не найден"));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return payment.getClient().getId().equals(currentUser.getId());
    }
}