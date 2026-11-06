package org.example.eventpro.dto.reservationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventpro.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationResponse {
    private Long id;
    private ReservationStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime dateExpiration;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private UUID clientId;
    private List<ReservationLineResponse> lines;
}