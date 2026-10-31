package org.example.eventpro.dto.eventDto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventSalesResponse {
    private Long eventId;
    private String title;
    private int totalCapacity;
    private int ticketsSold;
    private int placesReserved;
    private int placesAvailable;
    private BigDecimal grossRevenue;
}
