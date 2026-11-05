package org.example.eventpro.dto.promotionDto;

import lombok.*;
import org.example.eventpro.entity.enums.ReductionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionResponse {
    private Long id;
    private String code;
    private ReductionType reductionType;
    private BigDecimal reductionValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int usageLimit;
    private int usageCount;
    private Long ticketCategoryId;
    private String ticketCategoryName;
}
