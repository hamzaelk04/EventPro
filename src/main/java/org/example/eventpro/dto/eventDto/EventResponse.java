package org.example.eventpro.dto.eventDto;

import lombok.*;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryResponse;
import org.example.eventpro.entity.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime saleOpeningDate;
    private LocalDateTime saleClosingDate;
    private EventStatus status;
    private UUID organizerId;
    private List<TicketCategoryResponse> categories;
}