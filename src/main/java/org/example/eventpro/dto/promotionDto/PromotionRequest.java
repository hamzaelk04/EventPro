package org.example.eventpro.dto.promotionDto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.eventpro.entity.enums.ReductionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionRequest {
    @NotBlank(message = "Le code promo est obligatoire")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    private String code;

    @NotNull(message = "Le type de réduction est obligatoire")
    private ReductionType reductionType;

    @NotNull(message = "La valeur de la réduction est obligatoire")
    @DecimalMin(value = "0.01", message = "La valeur de la réduction doit être supérieure à 0")
    @Digits(integer = 10, fraction = 2, message = "La valeur de la réduction doit avoir au plus 2 décimales")
    private BigDecimal reductionValue;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotNull(message = "La limite d'utilisation est obligatoire")
    @Min(value = 1, message = "La limite d'utilisation doit être d'au moins 1")
    private Integer usageLimit;
}
