package org.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_category")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_places", nullable = false)
    @Builder.Default
    private int reservedPlaces = 0;

    @Column(name = "sold_places", nullable = false)
    @Builder.Default
    private int soldPlaces = 0;

    private Integer quotaMaxPerClient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    public int availablePlaces() {
        return totalQuantity - (reservedPlaces + soldPlaces);
    }

    public boolean checkStock(int quantity) {
        return quantity > 0 && quantity <= availablePlaces();
    }

    public void reserve(int quantity) {
        reservedPlaces += quantity;
    }

//  Libere reservation
//    confirmer vente
//    liberer vente
}
