package org.example.eventpro.dto.reservationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationLineResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private String promoCode;
}