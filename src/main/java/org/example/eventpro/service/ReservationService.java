package org.example.eventpro.service;

import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.reservationDto.ReservationLineRequest;
import org.example.eventpro.dto.reservationDto.ReservationRequest;
import org.example.eventpro.dto.reservationDto.ReservationResponse;
import org.example.eventpro.entity.*;
import org.example.eventpro.entity.enums.EventStatus;
import org.example.eventpro.entity.enums.ReservationStatus;
import org.example.eventpro.exception.eventException.InvalidEventStateException;
import org.example.eventpro.exception.reservationException.InsufficientStockException;
import org.example.eventpro.exception.reservationException.InvalidReservationStateException;
import org.example.eventpro.exception.userException.ResourceNotFoundException;
import org.example.eventpro.exception.userException.ValidationException;
import org.example.eventpro.mapper.ReservationMapper;
import org.example.eventpro.repository.ClientRepository;
import org.example.eventpro.repository.PromotionRepository;
import org.example.eventpro.repository.ReservationRepository;
import org.example.eventpro.repository.TicketCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final List<ReservationStatus> QUOTA_STATUSES = List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT);

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final TicketCategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final ReservationMapper reservationMapper;

//    CREATE
    @Transactional
    public ReservationResponse create(UUID clientId, ReservationRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<ReservationLineRequest> lines = request.getLines().stream()
                .sorted(Comparator.comparing(ReservationLineRequest::getCategoryId))
                .toList();
       ensureNoDuplicateCategory(lines);

        Map<Long, TicketCategory> categories = lockCategories(lines);

        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = Reservation.builder()
                .client(client)
                .creationDate(now)
                .dateExpiration(now.plusMinutes(10))
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        for (ReservationLineRequest lineRequest : lines) {
            TicketCategory category = categories.get(lineRequest.getCategoryId());
            int quantity = lineRequest.getQuantity();

            ensureCategoryIsOnSale(category, now);
            ensureStock(category, quantity);
            ensureQuota(clientId, category, quantity);

            category.reserve(quantity);

            ReservationLine line = ReservationLine.builder()
                    .category(category)
                    .quantity(quantity)
                    .unitPrice(category.getUnitPrice())
                    .build();

            line.calculateSubTotal();

            applyPromoCode(line, category.getId(), lineRequest.getPromoCode(), now);

            reservation.addLine(line);
        }

        reservation.recalculateAmounts();

        Reservation saved = reservationRepository.save(reservation);

        return reservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getOwned(Long reservationId, UUID clientId) {
        Reservation reservation = reservationRepository.findByIdAndClientId(reservationId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", reservationId));
        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listByClient(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
        return reservationMapper.toResponseList(
                reservationRepository.findByClientIdOrderByCreationDateDesc(clientId));
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId, UUID clientId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", reservationId));
        if (!reservation.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Réservation", reservationId);
        }
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new InvalidReservationStateException(
                    "Seule une réservation en attente de paiement peut être annulée (statut actuel : "
                            + reservation.getStatus() + ")");
        }

        releaseReservation(reservation);
        reservation.annuler();
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void releaseReservation(Reservation reservation) {
        List<ReservationLine> lines = reservation.getReservationLines().stream()
                .sorted(Comparator.comparing(line -> line.getCategory().getId()))
                .toList();

        for (ReservationLine line : lines) {
            Long categoryId = line.getCategory().getId();
            TicketCategory category = categoryRepository.findByIdForUpdate(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));
            category.releaseReservation(line.getQuantity());
        }

        for (ReservationLine line : lines) {
            if (line.getPromotion() != null) {
                Long promotionId = line.getPromotion().getId();
                Promotion promotion = promotionRepository.findByIdForUpdate(promotionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Promotion", promotionId));
                promotion.decrementUsage();
            }
        }
    }

//    HELPERS
    private void ensureNoDuplicateCategory(List<ReservationLineRequest> lines) {
        Set<Long> seen = new HashSet<>();

        for (ReservationLineRequest line: lines) {
            if (!seen.add(line.getCategoryId())) throw new ValidationException(
                    "La catégorie " + line.getCategoryId() + " apparaît plusieurs fois dans la réservation");
        }
    }

    private Map<Long, TicketCategory> lockCategories(List<ReservationLineRequest> lines) {
        Map<Long, TicketCategory> categories = new TreeMap<>();
        for (ReservationLineRequest line : lines) {
            Long categoryId = line.getCategoryId();
            TicketCategory category = categoryRepository.findByIdForUpdate(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categoryId));
            categories.put(categoryId, category);
        }
        return categories;
    }

    private void ensureCategoryIsOnSale(TicketCategory category, LocalDateTime now) {
        Event event = category.getEvent();

        if (event.getStatus() != EventStatus.PUBLISHED) throw new InvalidEventStateException(
                "L'événement « " + event.getTitle() + " » n'est pas ouvert à la réservation (statut : "
                        + event.getStatus() + ")");

        if (event.getSaleOpeningDate() != null && now.isBefore(event.getSaleOpeningDate())) throw new InvalidEventStateException(
                "La vente de l'événement « " + event.getTitle() + " » n'est pas encore ouverte");

        if (event.getSaleClosingDate() != null && now.isAfter(event.getSaleClosingDate())) throw new InvalidEventStateException(
                "La vente de l'événement « " + event.getTitle() + " » est terminée");

        if (!now.isBefore(event.getStartDate())) throw new InvalidEventStateException(
                "L'événement « " + event.getTitle() + " » a déjà commencé");
    }

    private void ensureStock(TicketCategory category, int quantity) {
        if (!category.checkStock(quantity)) throw new InsufficientStockException(category.getName(), quantity, category.availablePlaces());
    }

    private void ensureQuota(UUID clientId, TicketCategory category, int quantity) {
        Integer quota = category.getQuotaMaxPerClient();

        if (quota == null) return;

        long alreadyHeld = reservationRepository.sumQuantityByClientAndCategory(
                clientId, category.getId(), QUOTA_STATUSES
        );

        if (alreadyHeld + quantity > quota) throw new ValidationException(
                "Quota dépassé pour la catégorie « " + category.getName() + " » : maximum " + quota
                        + " place(s) par client, déjà réservée(s) : " + alreadyHeld);
    }

    private void applyPromoCode(ReservationLine line, Long categoryId, String promoCode, LocalDateTime now) {
        if (promoCode == null || promoCode.isBlank()) return;

        String code = promoCode.trim();

        Promotion promotion = promotionRepository
                .findByTicketCategoryIdAndCodeIgnoreCase(categoryId, code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Code promo « " + code + " » introuvable pour la catégorie " + categoryId));

        if (!promotion.isValidAt()) throw new ValidationException(
                "Le code promo « " + code + " » n'est pas valide (expiré ou limite d'utilisation atteinte)");

        line.applyPromotion(promotion);

        promotion.incrementUsage();
    }
}
