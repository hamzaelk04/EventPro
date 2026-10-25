package org.example.eventpro.entity;

import org.example.eventpro.entity.enums.ReductionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReductionType reductionType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal reductionValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer limitUse;

    @Column(nullable = false)
    @Builder.Default
    private int numberUse = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    public void increamentNumberUse() {
        numberUse++;
    }

//    est validee
//    calculer reduction
}
