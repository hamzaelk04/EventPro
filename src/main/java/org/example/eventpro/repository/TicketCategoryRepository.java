package org.example.eventpro.repository;

import org.example.eventpro.entity.TicketCategory;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository {
    List<TicketCategory> findByEventIdOrderByIdAsc(Long eventId);

    Optional<TicketCategory> findByIdAndEventId(Long id, Long eventId);

    boolean existsByEventIdAndNameIgnoreCase(Long eventId, String name);

    boolean existsByEventIdAndNameIgnoreCaseAndIdNot(Long eventId, String name, Long id);

    Optional<TicketCategory> findByIdAndEventIdForUpdate(Long id, Long eventId);

    Optional<TicketCategory> findByIdForUpdate(Long id);
}
