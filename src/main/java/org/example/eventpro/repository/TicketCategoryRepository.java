package org.example.eventpro.repository;

import jakarta.persistence.LockModeType;
import org.example.eventpro.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    List<TicketCategory> findByEventIdOrderByIdAsc(Long eventId);

    Optional<TicketCategory> findByIdAndEventId(Long id, Long eventId);

    boolean existsByEventIdAndNameIgnoreCase(Long eventId, String name);

    boolean existsByEventIdAndNameIgnoreCaseAndIdNot(Long eventId, String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM TicketCategory c WHERE c.id = :id AND c.event.id = :eventId")
    Optional<TicketCategory> findByIdAndEventIdForUpdate(@Param("id") Long id, @Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM TicketCategory c WHERE c.id = :id")
    Optional<TicketCategory> findByIdForUpdate(Long id);
}
