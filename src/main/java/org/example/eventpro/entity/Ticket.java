package org.example.eventpro.entity;

import org.example.eventpro.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "issue_date", nullable = false)
    @Builder.Default
    private LocalDateTime issueDate = LocalDateTime.now();

    @Column(name = "control_date")
    private LocalDateTime controlDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.VALID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_line_id")
    private ReservationLine reservationLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    public boolean valider() {
        return status == TicketStatus.VALID;
    }

//    Marks the ticket as used. Returns false if it was not VALID

    public void annuler() {
        this.status = TicketStatus.CANCELLED;
    }
}
