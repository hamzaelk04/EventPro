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
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

//    cascade, orphanRemoval
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReservationLine> reservationLines = new ArrayList<>();

    @OneToOne(mappedBy = "reservation")
    private Payment payments;

//    Ajouter une ligne
    public void addLine(ReservationLine line) {
        reservationLines.add(line);
//        ajouter dans reservation line
    }

//    Calculer le total
//    check if it is expired

    public void confirmer() { this.status = ReservationStatus.CONFIRMED; }
    public void annuler()   { this.status = ReservationStatus.CANCELLED; }
    public void expirer()   { this.status = ReservationStatus.EXPIRED; }
    public void rembourser(){ this.status = ReservationStatus.REFUNDED; }
}
