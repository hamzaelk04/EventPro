package org.example.eventpro.dto.ticketCategoryDto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketCategoryResponse {
    private Long id;
    private String name;
    private BigDecimal unitPrice;
    private int totalQuantity;
    private int availablePlaces;
    private Integer quotaMaxPerClient;
}
