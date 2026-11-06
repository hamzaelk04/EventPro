package org.example.eventpro.repository;

import jakarta.persistence.LockModeType;
import org.example.eventpro.entity.Reservation;
import org.example.eventpro.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndClientId(Long id, UUID clientId);

    List<Reservation> findByClientIdOrderByCreationDateDesc(UUID clientId);

    List<Reservation> findByStatusAndDateExpirationBefore(ReservationStatus status, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT COALESCE(SUM(l.quantity), 0)
            FROM ReservationLine l
            WHERE l.reservation.client.id = :clientId
            AND l.category.id = :categoryId
            AND l.reservation.status IN :statuses
            """)
    long sumQuantityByClientAndCategory(@Param("clientId") UUID clientId,
                                        @Param("categoryId") Long categoryId,
                                        @Param("statuses") List<ReservationStatus> statuses);
}
