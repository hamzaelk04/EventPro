package org.example.eventpro.service;

import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.promotionDto.PromotionRequest;
import org.example.eventpro.dto.promotionDto.PromotionResponse;
import org.example.eventpro.entity.Event;
import org.example.eventpro.entity.Organizer;
import org.example.eventpro.entity.Promotion;
import org.example.eventpro.entity.TicketCategory;
import org.example.eventpro.entity.enums.EventStatus;
import org.example.eventpro.entity.enums.ReductionType;
import org.example.eventpro.exception.eventException.InvalidEventStateException;
import org.example.eventpro.exception.userException.ResourceNotFoundException;
import org.example.eventpro.exception.userException.ValidationException;
import org.example.eventpro.mapper.PromotionMapper;
import org.example.eventpro.repository.PromotionRepository;
import org.example.eventpro.repository.TicketCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final TicketCategoryRepository categoryRepository;

    @Transactional
    public PromotionResponse create(Long eventId, Long categoryId, UUID organizerId, PromotionRequest request) {
        TicketCategory category = getOwnedCategory(eventId, categoryId, organizerId);
        ensureEventAcceptsPromotionChanges(category.getEvent());
        validateDates(request);
        validateReduction(request);

        String code = normalizeCode(request.getCode());

        if (promotionRepository.existsByTicketCategoryIdAndCodeIgnoreCase(categoryId, code))
            throw new ValidationException("Le code promo « " + code + " » existe déjà");

        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setCode(code);
        promotion.setTicketCategory(category);

        Promotion saved = promotionRepository.save(promotion);

        return promotionMapper.toResponse(saved);
    }

    @Transactional
    public PromotionResponse update(Long eventId, Long categoryId, Long promotionId, UUID organizerId,
                                    PromotionRequest request) {
        Promotion promotion = promotionRepository.findByIdForUpdate(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", promotionId));
        ensureBelongsTo(promotion, eventId, categoryId, organizerId);
        ensureEventAcceptsPromotionChanges(promotion.getTicketCategory().getEvent());
        validateDates(request);
        validateReduction(request);

        String code = normalizeCode(request.getCode());
        if (promotionRepository.existsByTicketCategoryIdAndCodeIgnoreCaseAndIdNot(categoryId, code, promotionId)) {
            throw new ValidationException("Le code promo « " + code + " » existe déjà pour cette catégorie");
        }

        if (request.getUsageLimit() < promotion.getUsageCount()) {
            throw new InvalidEventStateException(
                    "La limite d'utilisation ne peut pas être inférieure au nombre d'utilisations déjà effectuées ("
                            + promotion.getUsageCount() + ")");
        }

        promotionMapper.updateEntity(request, promotion);
        promotion.setCode(code);
        return promotionMapper.toResponse(promotion);
    }

    @Transactional
    public void delete(Long eventId, Long categoryId, Long promotionId, UUID organizerId) {
        Promotion promotion = findOwnedPromotion(eventId, categoryId, promotionId, organizerId);
        if (promotion.getUsageCount() > 0) {
            throw new InvalidEventStateException("Une promotion déjà utilisée ne peut pas être supprimée");
        }
        promotionRepository.delete(promotion);
    }

    @Transactional(readOnly = true)
    public PromotionResponse getOwnedPromotion(Long eventId, Long categoryId, Long promotionId, UUID organizerId) {
        return promotionMapper.toResponse(findOwnedPromotion(eventId, categoryId, promotionId, organizerId));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> listByCategory(Long eventId, Long categoryId, UUID organizerId) {
        getOwnedCategory(eventId, categoryId, organizerId);
        return promotionMapper.toResponseList(promotionRepository.findByTicketCategoryId(categoryId));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> listByOrganizer(UUID organizerId) {
        return promotionMapper.toResponseList(promotionRepository.findByTicketCategoryEventOrganizerId(organizerId));
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionEntity(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", id));
    }


    //    HELPERS
    private Promotion findOwnedPromotion(Long eventId, Long categoryId, Long promotionId, UUID organizerId) {
        Promotion promotion = promotionRepository.findByIdAndTicketCategoryEventOrganizerId(promotionId, organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", promotionId));
        ensureBelongsTo(promotion, eventId, categoryId, organizerId);
        return promotion;
    }

    private TicketCategory getOwnedCategory(Long eventId, Long categoryId, UUID organizerId) {
        TicketCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));

        Event event = category.getEvent();
        Organizer organizer = category.getEvent().getOrganizer();

        if (organizer.getId().equals(organizerId) && event.getId().equals(eventId)) return category;

        throw new ResourceNotFoundException("Catégorie", categoryId);
    }

    private void ensureBelongsTo(Promotion promotion, Long eventId, Long categoryId, UUID organizerId) {
        TicketCategory category = promotion.getTicketCategory();
        Event event = category.getEvent();
        if (!category.getId().equals(categoryId)
                || !event.getId().equals(eventId)
                || !event.getOrganizer().getId().equals(organizerId)) {
            throw new ResourceNotFoundException("Promotion", promotion.getId());
        }
    }

    private void ensureOwner(Promotion promotion, UUID organizerId, Long promotionId) {
        UUID ownerId = promotion.getTicketCategory().getEvent().getOrganizer().getId();

        if (!ownerId.equals(organizerId)) throw new ResourceNotFoundException("Promotion", promotionId);
    }

    private void ensureEventAcceptsPromotionChanges(Event event) {
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidEventStateException(
                    "Les promotions ne peuvent plus être modifiées pour un événement au statut " + event.getStatus());
        }
    }

    private void validateDates(PromotionRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ValidationException("La date de fin doit être postérieure à la date de début");
        }
    }

    private void validateReduction(PromotionRequest request) {
        if (request.getReductionType() == ReductionType.PERCENTAGE
                && request.getReductionValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("Un pourcentage de réduction ne peut pas dépasser 100");
        }
    }

    private String normalizeCode(String code) {
        return code.toUpperCase(Locale.ROOT);
    }
}
