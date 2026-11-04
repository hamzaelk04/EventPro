package org.example.eventpro.service;

import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryResponse;
import org.example.eventpro.entity.Event;
import org.example.eventpro.entity.TicketCategory;
import org.example.eventpro.entity.enums.EventStatus;
import org.example.eventpro.exception.eventException.InvalidEventStateException;
import org.example.eventpro.exception.userException.ResourceNotFoundException;
import org.example.eventpro.exception.userException.ValidationException;
import org.example.eventpro.mapper.TicketCategoryMapper;
import org.example.eventpro.repository.EventRepository;
import org.example.eventpro.repository.TicketCategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {
    private final TicketCategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryMapper categoryMapper;

//    Add a new ticket category
    @Transactional
    public TicketCategoryResponse add(Long eventId, UUID organizerId, TicketCategoryRequest request) {
        Event event = getOwnedEvent(eventId, organizerId);
        ensureEventAcceptsCategoryChanges(event);
        validateQuota(request);

        String name = request.getName().trim();
        if (categoryRepository.existsByEventIdAndNameIgnoreCase(eventId, name)) {
            throw new ValidationException("Une catégorie nommée « " + name + " » existe déjà pour cet événement");
        }

        TicketCategory category = categoryMapper.toEntity(request);
        category.setName(name);
        event.addTicketCategory(category);
        TicketCategory saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

//    Update
    @Transactional
    public TicketCategoryResponse update(Long eventId, Long categoryId, UUID organizerId, TicketCategoryRequest request) {
        Event event = getOwnedEvent(eventId, organizerId);
        ensureEventAcceptsCategoryChanges(event);
        validateQuota(request);

        TicketCategory category = categoryRepository.findByIdAndEventIdForUpdate(categoryId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));

        String name = request.getName().trim();
        if (categoryRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(eventId, name, categoryId)) {
            throw new ValidationException("Une catégorie nommée « " + name + " » existe déjà pour cet événement");
        }

        int alreadyTaken = category.getReservedPlaces() + category.getSoldPlaces();
        if (request.getTotalQuantity() < alreadyTaken) {
            throw new InvalidEventStateException(
                    "La quantité totale ne peut pas être inférieure aux places déjà réservées ou vendues ("
                            + alreadyTaken + ")");
        }

        categoryMapper.updateEntity(request, category);
        category.setName(name);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long eventId, Long categoryId, UUID organizerId) {
        Event event = getOwnedEvent(eventId, organizerId);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStateException(
                    "Une catégorie ne peut être supprimée que tant que l'événement est en brouillon");
        }

        TicketCategory category = categoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));

        event.removeCategory(category);
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> listByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));

        if (event.getStatus() == EventStatus.DRAFT) {
            throw new ResourceNotFoundException("Événement", eventId);
        }

        return categoryMapper.toResponseList(categoryRepository.findByEventIdOrderByIdAsc(eventId));
    }

    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> listOwnedByEvent(Long eventId, UUID organizerId) {
        getOwnedEvent(eventId, organizerId);
        return categoryMapper.toResponseList(categoryRepository.findByEventIdOrderByIdAsc(eventId));
    }

    @Transactional(readOnly = true)
    public TicketCategoryResponse getOwnedCategory(Long eventId, Long categoryId, UUID organizerId) {
        getOwnedEvent(eventId, organizerId);
        TicketCategory category = categoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public TicketCategory getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", id));
    }

//  Helpers
    private Event getOwnedEvent(Long eventId, UUID organizerId) {
        return eventRepository.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));
    }

    private void ensureEventAcceptsCategoryChanges(Event event) {
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED) {
            throw new InvalidEventStateException(
                    "Les catégories ne peuvent plus être modifiées pour un événement au statut " + event.getStatus()
            );
        }
    }

    private void validateQuota(TicketCategoryRequest request) {
        Integer quota = request.getQuotaMaxPerClient();
        if (quota != null && quota > request.getTotalQuantity()) {
            throw new ValidationException("Le quota maximum par client ne peut pas dépasser la quantité totale");
        }
    }
}
