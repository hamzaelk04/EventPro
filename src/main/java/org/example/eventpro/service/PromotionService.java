package org.example.eventpro.service;

import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.promotionDto.PromotionRequest;
import org.example.eventpro.dto.promotionDto.PromotionResponse;
import org.example.eventpro.entity.Event;
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
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final TicketCategoryRepository categoryRepository;

    @Transactional
    public PromotionResponse create(UUID organizerId, PromotionRequest request) {
        TicketCategory category = getOwnedCategory(request.getTicketCategoryId(), organizerId);
        ensureEventAcceptsPromotionChanges(category.getEvent());
        validateDates(request);
        validateReduction(request);

        String code = normalizeCode(request.getCode());

        if (promotionRepository.existsByTicketCategoryIdAndCodeIgnoreCase(category.getId(), code)) throw new ValidationException("Le code promo « " + code + " » existe déjà");

        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setCode(code);
        promotion.setTicketCategory(category);

        Promotion saved = promotionRepository.save(promotion);

        return promotionMapper.toResponse(saved);
    }


//    HELPERS
    private TicketCategory getOwnedCategory(Long categoryId, UUID organizerId) {
        TicketCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));

        if (category.getEvent().getOrganizer().getId().equals(organizerId)) return category;

        throw new ResourceNotFoundException("Catégorie", categoryId);
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
