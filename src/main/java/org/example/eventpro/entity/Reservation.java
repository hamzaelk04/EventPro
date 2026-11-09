package org.example.eventpro.entity;

import org.example.eventpro.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateExpiration = LocalDateTime.now().plusMinutes(10);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    //    cascade, orphanRemoval
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReservationLine> reservationLines = new ArrayList<>();

    @OneToMany(mappedBy = "reservation")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    //    Ajouter une ligne
    public void addLine(ReservationLine line) {
        reservationLines.add(line);
//        ajouter dans reservation line
    }

    public void recalculateAmounts() {
        this.subTotal = reservationLines.stream()
                .map(ReservationLine::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.discountAmount = reservationLines.stream()
                .map(ReservationLine::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //    Calculer le total
    public BigDecimal totalAmount() {
        return subTotal.subtract(discountAmount).max(BigDecimal.ZERO);
    }

    //    check if it is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateExpiration);
    }

    public void confirmer() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void annuler() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void expirer() {
        this.status = ReservationStatus.EXPIRED;
    }

    public void rembourser() {
        this.status = ReservationStatus.REFUNDED;
    }
}
