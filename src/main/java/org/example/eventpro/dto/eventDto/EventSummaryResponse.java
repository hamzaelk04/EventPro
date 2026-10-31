package org.example.eventpro.dto.eventDto;

import lombok.*;
import org.example.eventpro.entity.enums.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventSummaryResponse {
    private Long id;
    private String title;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;
    private BigDecimal minPrice;
    private int totalAvailablePlaces;
}