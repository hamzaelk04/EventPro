package org.example.eventpro.service;

import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.eventDto.*;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;
import org.example.eventpro.dto.userDto.PageResponse;
import org.example.eventpro.entity.Event;
import org.example.eventpro.entity.Organizer;
import org.example.eventpro.entity.TicketCategory;
import org.example.eventpro.entity.enums.EventStatus;
import org.example.eventpro.exception.eventException.InvalidEventStateException;
import org.example.eventpro.exception.userException.ResourceNotFoundException;
import org.example.eventpro.exception.userException.ValidationException;
import org.example.eventpro.mapper.EventMapper;
import org.example.eventpro.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;

    //    CREATE
    @Transactional
    public EventResponse create(EventCreateRequest request) {
        Organizer organizer = userService.getOrganizerEntity(request.getOrganizerId());

        validateDates(request.getStartDate(),
                request.getEndDate(),
                request.getSaleOpeningDate(),
                request.getSaleClosingDate());
        validateUniqueCategoryNames(request.getCategories());

        Event event = eventMapper.toEntity(request);
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(organizer);
        event.setTitle(request.getTitle().trim());

        for (TicketCategoryRequest categoryRequest : request.getCategories()) {
            TicketCategory category = eventMapper.toEntity(categoryRequest);
            category.setName(categoryRequest.getName().trim());
            event.addTicketCategory(category);
        }

        return eventMapper.toResponse(eventRepository.save(event));
    }

    //    UPDATE
    @Transactional
    public EventResponse update(Long eventId, UUID organizerId, EventUpdateRequest request) {
        Event event = findOwnedOrThrow(eventId, organizerId);

        switch (event.getStatus()) {
            case DRAFT -> {
                validateDates(request.getStartDate(), request.getEndDate(),
                        request.getSaleOpeningDate(), request.getSaleClosingDate());
                if (!request.getStartDate().isAfter(LocalDateTime.now())) {
                    throw new ValidationException("La date de début doit être dans le futur");
                }
                eventMapper.updateEntity(request, event);
            }
            case PUBLISHED -> {
                if (datesChanged(event, request)) {
                    throw new InvalidEventStateException(
                            "Les dates ne sont plus modifiables après la publication");
                }
                event.setTitle(request.getTitle());
                event.setDescription(request.getDescription());
                event.setAddress(request.getAddress());
            }
            default -> throw new InvalidEventStateException(
                    "Un événement " + event.getStatus() + " ne peut plus être modifié");
        }

        event.setTitle(event.getTitle().trim());

        return eventMapper.toResponse(event);
    }

//    PUBLISH

    @Transactional
    public EventResponse publish(Long eventId, UUID organizerId) {
        Event event = findOwnedOrThrow(eventId, organizerId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStateException(
                    "Seul un événement en brouillon peut être publié (statut actuel : "
                            + event.getStatus() + ")");
        }
        if (event.getCategories().isEmpty()) {
            throw new ValidationException("Un événement doit avoir au moins une catégorie de billets");
        }
        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Impossible de publier un événement déjà commencé");
        }

        event.setStatus(EventStatus.PUBLISHED);
        return eventMapper.toResponse(event);
    }

//    CANCEL

    @Transactional
    public EventResponse cancel(Long eventId, UUID organizerId) {
        Event event = findOwnedOrThrow(eventId, organizerId);

        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED) {
            throw new InvalidEventStateException(
                    "Un événement " + event.getStatus() + " ne peut pas être annulé");
        }

        event.setStatus(EventStatus.CANCELLED);
        // TODO (payment layer): automatically refund every buyer at 100% and invalidate their tickets
        return eventMapper.toResponse(event);
    }

//    READ

    @Transactional(readOnly = true)
    public PageResponse<EventSummaryResponse> listCatalogue(int page, int size) {
        Pageable pageable = pageable(page, size, Sort.by("startDate").ascending());
        Page<Event> result = eventRepository.findByStatusAndStartDateAfter(
                EventStatus.PUBLISHED, LocalDateTime.now(), pageable);
        return PageResponse.from(result.map(eventMapper::toSummary));
    }

    /**
     * Public detail: drafts are hidden
     */
    @Transactional(readOnly = true)
    public EventResponse getPublicEvent(Long id) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.getStatus() != EventStatus.DRAFT)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", id));
        return eventMapper.toResponse(event);
    }

    /**
     * An organizer's own events, any status
     */
    @Transactional(readOnly = true)
    public PageResponse<EventSummaryResponse> listByOrganizer(UUID organizerId, int page, int size) {
        userService.getOrganizerEntity(organizerId);   // 404 if the organizer doesn't exist
        Pageable pageable = pageable(page, size, Sort.by("startDate").descending());
        Page<Event> result = eventRepository.findByOrganizerId(organizerId, pageable);
        return PageResponse.from(result.map(eventMapper::toSummary));
    }

    /**
     * Owner's detail view, any status (including drafts)
     */
    @Transactional(readOnly = true)
    public EventResponse getOwnedEvent(Long eventId, UUID organizerId) {
        return eventMapper.toResponse(findOwnedOrThrow(eventId, organizerId));
    }

    /**
     * US7: sales summary (sold counts stay at 0 until the payment layer exists)
     */
    @Transactional(readOnly = true)
    public EventSalesResponse getSales(Long eventId, UUID organizerId) {
        return eventMapper.toSales(findOwnedOrThrow(eventId, organizerId));
    }

    // ENTITY LOOKUP (used later by the reservation service)

    @Transactional(readOnly = true)
    public Event getEventEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", id));
    }


    //    HELPERS

    private Event findOwnedOrThrow(Long eventId, UUID organizerId) {
        return eventRepository.findByIdAndOrganizerId(organizerId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end,
                               LocalDateTime saleOpening, LocalDateTime saleClosing) {
        if (!end.isAfter(start)) {
            throw new ValidationException("La date de fin doit être après la date de début");
        }
        if (saleOpening != null && !saleOpening.isBefore(start)) {
            throw new ValidationException("L'ouverture des ventes doit précéder le début de l'événement");
        }
        if (saleClosing != null && saleClosing.isAfter(start)) {
            throw new ValidationException("La clôture des ventes ne peut pas dépasser le début de l'événement");
        }
        if (saleOpening != null && saleClosing != null && !saleOpening.isBefore(saleClosing)) {
            throw new ValidationException("L'ouverture des ventes doit précéder la clôture des ventes");
        }
    }

    private void validateUniqueCategoryNames(List<TicketCategoryRequest> categories) {
        Set<String> names = new HashSet<>();
        for (TicketCategoryRequest category : categories) {
            String normalized = category.getName().trim().toLowerCase(Locale.ROOT);
            if (!names.add(normalized)) {
                throw new ValidationException(
                        "Deux catégories portent le même nom : " + category.getName().trim());
            }
        }
    }

    private boolean datesChanged(Event event, EventUpdateRequest request) {
        return !Objects.equals(event.getStartDate(), request.getStartDate())
                || !Objects.equals(event.getEndDate(), request.getEndDate())
                || !Objects.equals(event.getSaleOpeningDate(), request.getSaleOpeningDate())
                || !Objects.equals(event.getSaleClosingDate(), request.getSaleClosingDate());
    }

    private Pageable pageable(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
