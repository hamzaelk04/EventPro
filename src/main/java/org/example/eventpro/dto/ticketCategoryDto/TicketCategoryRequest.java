package org.example.eventpro.dto.ticketCategoryDto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketCategoryRequest {
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.00", message = "Le prix ne peut pas être négatif")
    @Digits(integer = 10, fraction = 2, message = "Prix invalide (2 décimales maximum)")
    private BigDecimal unitPrice;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer totalQuantity;

    @Min(value = 1, message = "Le quota par client doit être d'au moins 1")
    private Integer quotaMaxPerClient;
}