package org.example.eventpro.entity;

import org.example.eventpro.entity.enums.RefaundStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refaunds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Refaund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String reason;

    @Column(name = "request_date", nullable = false)
    @Builder.Default
    private LocalDateTime requestDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefaundStatus status = RefaundStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    public void approuver() { this.status = RefaundStatus.APPROVED; }
    public void refuser()   { this.status = RefaundStatus.REJECTED; }
}
