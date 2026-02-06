package com.example.curs4.controller;

import com.example.curs4.dto.DeclarationDTO;
import com.example.curs4.exception.CustomException;
import com.example.curs4.service.DeclarationService;
import com.example.curs4.service.SecurityService; // ДОБАВЬТЕ
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
@Slf4j
public class DeclarationController {

    private final DeclarationService declarationService;
    private final SecurityService securityService; // ДОБАВЬТЕ

    @Operation(summary = "Создать декларацию")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<DeclarationDTO> createDeclaration(@Valid @RequestBody DeclarationDTO dto) {
        log.info("Создание декларации для клиента ID: {}", dto.getClientId());
        DeclarationDTO saved = declarationService.createDeclaration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Получить декларацию по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'DRIVER')")
    public ResponseEntity<DeclarationDTO> getDeclarationById(@PathVariable Long id) {
        log.info("Получение декларации по ID: {}", id);
        DeclarationDTO declaration = declarationService.getDeclarationById(id);
        return ResponseEntity.ok(declaration);
    }

    @Operation(summary = "Получить все декларации")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeclarationDTO>> getAllDeclarations() {
        log.info("Получение всех деклараций");
        List<DeclarationDTO> declarations = declarationService.getAllDeclarations();
        return ResponseEntity.ok(declarations);
    }

    @Operation(summary = "Получить декларации по клиенту")
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication)) or hasRole('DRIVER')")
    public ResponseEntity<List<DeclarationDTO>> getDeclarationsByClient(@PathVariable Long clientId) {
        log.info("Получение деклараций для клиента ID: {}", clientId);
        List<DeclarationDTO> declarations = declarationService.getDeclarationsByClientId(clientId);
        return ResponseEntity.ok(declarations);
    }

    @Operation(summary = "Получить декларации по статусу")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<List<DeclarationDTO>> getDeclarationsByStatus(@PathVariable String status) {
        log.info("Получение деклараций со статусом: {}", status);
        List<DeclarationDTO> declarations = declarationService.getDeclarationsByStatus(status);
        return ResponseEntity.ok(declarations);
    }

    @Operation(summary = "Обновить декларацию")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @declarationService.isDeclarationOwner(#id, authentication))")
    public ResponseEntity<DeclarationDTO> updateDeclaration(@PathVariable Long id,
                                                            @Valid @RequestBody DeclarationDTO dto) {
        log.info("Обновление декларации ID: {}", id);
        DeclarationDTO updated = declarationService.updateDeclaration(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Обновить статус декларации")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<DeclarationDTO> updateDeclarationStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        log.info("Обновление статуса декларации ID: {} на: {}", id, status);
        DeclarationDTO updated = declarationService.updateDeclarationStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить декларацию")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @declarationService.isDeclarationOwner(#id, authentication))")
    public ResponseEntity<Void> deleteDeclaration(@PathVariable Long id) {
        log.info("Удаление декларации ID: {}", id);
        declarationService.deleteDeclaration(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить статистику по клиенту")
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<Map<String, Long>> getClientStats(@PathVariable Long clientId) {
        log.info("Получение статистики для клиента ID: {}", clientId);
        long total = declarationService.getDeclarationsCountByClient(clientId);
        long pending = declarationService.getDeclarationsCountByClientAndStatus(clientId, "PENDING");
        long approved = declarationService.getDeclarationsCountByClientAndStatus(clientId, "APPROVED");

        return ResponseEntity.ok(Map.of(
                "totalDeclarations", total,
                "pendingDeclarations", pending,
                "approvedDeclarations", approved
        ));
    }

    // Exception Handlers...
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