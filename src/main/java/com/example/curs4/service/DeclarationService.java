package com.example.curs4.service;

import com.example.curs4.dto.DeclarationDTO;
import com.example.curs4.entity.Declaration;
import com.example.curs4.entity.User;
import com.example.curs4.exception.CustomException;
import com.example.curs4.mapper.DeclarationMapper;
import com.example.curs4.repository.DeclarationRepository;
import com.example.curs4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeclarationService {

    private final DeclarationRepository declarationRepository;
    private final UserRepository userRepository;
    private final DeclarationMapper declarationMapper;

    // CREATE
    public DeclarationDTO createDeclaration(DeclarationDTO dto) {
        log.info("Создание декларации для клиента ID: {}", dto.getClientId());

        validateDeclaration(dto);

        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new CustomException("Клиент не найден"));

        Declaration declaration = declarationMapper.toEntity(dto);
        declaration.setClient(client);
        declaration.setDeclarationNumber(generateDeclarationNumber());
        declaration.setSubmittedAt(LocalDateTime.now());

        // Устанавливаем значения по умолчанию
        if (declaration.getProductValue() == null) declaration.setProductValue(BigDecimal.ZERO);
        if (declaration.getNetWeight() == null) declaration.setNetWeight(BigDecimal.ZERO);
        if (declaration.getQuantity() == null) declaration.setQuantity(0);

        Declaration savedDeclaration = declarationRepository.save(declaration);
        log.info("Декларация создана: {}", savedDeclaration.getDeclarationNumber());

        return declarationMapper.toDto(savedDeclaration);
    }

    // READ
    @Transactional(readOnly = true)
    public DeclarationDTO getDeclarationById(Long id) {
        Declaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Декларация не найдена"));
        return declarationMapper.toDto(declaration);
    }

    @Transactional(readOnly = true)
    public List<DeclarationDTO> getAllDeclarations() {
        return declarationRepository.findAll().stream()
                .map(declarationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeclarationDTO> getDeclarationsByClientId(Long clientId) {
        return declarationRepository.findByClientId(clientId).stream()
                .map(declarationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeclarationDTO> getDeclarationsByStatus(String status) {
        return declarationRepository.findByStatus(status).stream()
                .map(declarationMapper::toDto)
                .collect(Collectors.toList());
    }

    // UPDATE
    public DeclarationDTO updateDeclaration(Long id, DeclarationDTO dto) {
        Declaration existingDeclaration = declarationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Декларация не найдена"));

        // Проверяем, можно ли редактировать (только PENDING)
        if (!"PENDING".equals(existingDeclaration.getStatus())) {
            throw new CustomException("Редактирование невозможно. Декларация уже обработана.");
        }

        // Обновляем разрешенные поля
        existingDeclaration.setDeclarationType(dto.getDeclarationType());
        existingDeclaration.setTnvedCode(dto.getTnvedCode());
        existingDeclaration.setProductDescription(dto.getProductDescription());
        existingDeclaration.setProductValue(dto.getProductValue());
        existingDeclaration.setNetWeight(dto.getNetWeight());
        existingDeclaration.setQuantity(dto.getQuantity());
        existingDeclaration.setCountryOfOrigin(dto.getCountryOfOrigin());
        existingDeclaration.setCountryOfDestination(dto.getCountryOfDestination());
        existingDeclaration.setCustomsOffice(dto.getCustomsOffice());

        Declaration updatedDeclaration = declarationRepository.save(existingDeclaration);
        log.info("Декларация обновлена: {}", updatedDeclaration.getDeclarationNumber());

        return declarationMapper.toDto(updatedDeclaration);
    }

    // UPDATE STATUS
    public DeclarationDTO updateDeclarationStatus(Long id, String status) {
        Declaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Декларация не найдена"));

        declaration.setStatus(status);

        if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
            declaration.setReviewedAt(LocalDateTime.now());
        }

        Declaration updatedDeclaration = declarationRepository.save(declaration);
        log.info("Статус декларации {} изменен на: {}",
                updatedDeclaration.getDeclarationNumber(), status);

        return declarationMapper.toDto(updatedDeclaration);
    }

    // DELETE
    public void deleteDeclaration(Long id) {
        Declaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Декларация не найдена"));

        // Проверяем, можно ли удалить (только PENDING для клиентов, админ может удалять любые)
        boolean isAdmin = false;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                isAdmin = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            }
        } catch (Exception e) {
            log.warn("Ошибка при проверке роли админа: {}", e.getMessage());
        }

        if (!isAdmin && !"PENDING".equals(declaration.getStatus())) {
            throw new CustomException("Удаление невозможно. Декларация уже обработана.");
        }

        declarationRepository.delete(declaration);
        log.info("Декларация удалена: {}", declaration.getDeclarationNumber());
    }

    // VALIDATION
    private void validateDeclaration(DeclarationDTO dto) {
        if (dto.getClientId() == null) {
            throw new CustomException("ID клиента обязателен");
        }

        if (dto.getDeclarationType() == null || dto.getDeclarationType().trim().isEmpty()) {
            throw new CustomException("Тип декларации обязателен");
        }

        if (dto.getProductDescription() == null || dto.getProductDescription().trim().isEmpty()) {
            throw new CustomException("Описание товара обязательно");
        }

        if (dto.getProductValue() == null) {
            throw new CustomException("Стоимость товара обязательна");
        }
        
        if (dto.getProductValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Стоимость товара не может быть отрицательной");
        }
    }

    // UTILITY METHODS
    private String generateDeclarationNumber() {
        String baseNumber = "TD-" + LocalDateTime.now().getYear() + "-";
        String declarationNumber;
        int attempt = 0;
        
        do {
            long count = declarationRepository.count() + 1 + attempt;
            declarationNumber = baseNumber + String.format("%05d", count);
            attempt++;
            
            // Защита от бесконечного цикла
            if (attempt > 1000) {
                declarationNumber = baseNumber + String.format("%05d", System.currentTimeMillis() % 100000);
                break;
            }
        } while (declarationRepository.existsByDeclarationNumber(declarationNumber));
        
        return declarationNumber;
    }

    @Transactional(readOnly = true)
    public boolean declarationExists(Long id) {
        return declarationRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long getDeclarationsCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return declarationRepository.countByClient(client);
    }

    @Transactional(readOnly = true)
    public long getDeclarationsCountByClientAndStatus(Long clientId, String status) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return declarationRepository.countByClientAndStatus(client, status);
    }
    public boolean isClientOwner(Long clientId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return currentUser.getId().equals(clientId);
    }

    public boolean isDeclarationOwner(Long declarationId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Declaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new CustomException("Декларация не найдена"));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return declaration.getClient().getId().equals(currentUser.getId());
    }
}