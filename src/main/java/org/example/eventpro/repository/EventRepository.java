package org.example.eventpro.repository;

import org.example.eventpro.entity.Event;
import org.example.eventpro.entity.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByStatusAndStartDateAfter(EventStatus status, LocalDateTime dateTime, Pageable pageable);

    Page<Event> findByOrganizerId(UUID id, Pageable pageable);

    Optional<Event> findByIdAndOrganizerId(Long id, UUID organizer_id);
}