package org.example.eventpro.dto.reservationDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationLineRequest {

    @NotNull(message = "La catégorie de billet est obligatoire")
    private Long categoryId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantity;

    @Size(max = 50, message = "Le code promo ne doit pas dépasser 50 caractères")
    private String promoCode;
}