package org.example.eventpro.entity;

import org.example.eventpro.entity.enums.ReductionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReductionType reductionType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal reductionValue;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_category_id", nullable = false)
    private TicketCategory ticketCategory;

    public void incrementUsage() {
        usageCount++;
    }

//    est validee
    public boolean isValidAt() {
        LocalDateTime now = LocalDateTime.now();
        boolean inPeriod = (startDate == null || !now.isBefore(startDate))
                && (endDate == null || !now.isAfter(endDate));

        boolean limitOk = usageLimit == null || usageCount < usageLimit;

        return inPeriod && limitOk;
    }
//    calculer reduction
    public BigDecimal calculateDiscount(BigDecimal lineSubtotal) {
        BigDecimal discount = (reductionType == ReductionType.PERCENTAGE)
                ? lineSubtotal.multiply(reductionValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : reductionValue;

        return discount.min(lineSubtotal).setScale(2, RoundingMode.HALF_UP);
    }
}
