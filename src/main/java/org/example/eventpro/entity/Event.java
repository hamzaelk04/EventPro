package org.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.eventpro.entity.enums.EventStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String address;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(name = "sale_opening_date")
    private LocalDateTime saleOpeningDate;

    @Column(name = "sale_colisng_date")
    private LocalDateTime saleClosingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

//    Add category
//    publier
//    annuler
}
