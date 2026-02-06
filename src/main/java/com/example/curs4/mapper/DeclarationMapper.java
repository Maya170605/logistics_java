package com.example.curs4.mapper;

import com.example.curs4.dto.DeclarationDTO;
import com.example.curs4.entity.Declaration;
import org.springframework.stereotype.Component;

@Component
public class DeclarationMapper {

    public DeclarationDTO toDto(Declaration declaration) {
        if (declaration == null) {
            return null;
        }

        String clientName = null;
        if (declaration.getClient() != null) {
            clientName = declaration.getClient().getName();
            if (clientName == null) {
                clientName = declaration.getClient().getUsername();
            }
        }

        return DeclarationDTO.builder()
                .id(declaration.getId())
                .declarationNumber(declaration.getDeclarationNumber())
                .clientId(declaration.getClient() != null ? declaration.getClient().getId() : null)
                .clientName(clientName)
                .declarationType(declaration.getDeclarationType())
                .tnvedCode(declaration.getTnvedCode())
                .productDescription(declaration.getProductDescription())
                .productValue(declaration.getProductValue())
                .netWeight(declaration.getNetWeight())
                .quantity(declaration.getQuantity())
                .countryOfOrigin(declaration.getCountryOfOrigin())
                .countryOfDestination(declaration.getCountryOfDestination())
                .customsOffice(declaration.getCustomsOffice())
                .status(declaration.getStatus())
                .submittedAt(declaration.getSubmittedAt())
                .reviewedAt(declaration.getReviewedAt())
                .createdAt(declaration.getCreatedAt())
                .updatedAt(declaration.getUpdatedAt())
                .build();
    }

    public Declaration toEntity(DeclarationDTO dto) {
        if (dto == null) {
            return null;
        }

        return Declaration.builder()
                .id(dto.getId())
                .declarationNumber(dto.getDeclarationNumber())
                .declarationType(dto.getDeclarationType())
                .tnvedCode(dto.getTnvedCode())
                .productDescription(dto.getProductDescription())
                .productValue(dto.getProductValue())
                .netWeight(dto.getNetWeight())
                .quantity(dto.getQuantity())
                .countryOfOrigin(dto.getCountryOfOrigin())
                .countryOfDestination(dto.getCountryOfDestination())
                .customsOffice(dto.getCustomsOffice())
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .submittedAt(dto.getSubmittedAt())
                .reviewedAt(dto.getReviewedAt())
                .build();
    }
}