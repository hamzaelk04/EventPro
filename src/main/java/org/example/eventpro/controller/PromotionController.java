package org.example.eventpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.promotionDto.PromotionRequest;
import org.example.eventpro.dto.promotionDto.PromotionResponse;
import org.example.eventpro.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Manage the promotions of a ticket category")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/events/{eventId}/categories/{categoryId}/promotions")
    @Operation(summary = "Add a promotion to a ticket category",
            description = "The code is stored in uppercase and must be unique within the category.")
    public ResponseEntity<PromotionResponse> create(@PathVariable Long eventId,
                                                    @PathVariable Long categoryId,
                                                    @RequestParam UUID organizerId,
                                                    @Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.create(eventId, categoryId, organizerId, request);
        URI location = URI.create("/api/events/" + eventId + "/categories/" + categoryId
                + "/promotions/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/events/{eventId}/categories/{categoryId}/promotions/{promotionId}")
    @Operation(summary = "Update a promotion",
            description = "Full replacement. The usage limit cannot go below the current usage count.")
    public ResponseEntity<PromotionResponse> update(@PathVariable Long eventId,
                                                    @PathVariable Long categoryId,
                                                    @PathVariable Long promotionId,
                                                    @RequestParam UUID organizerId,
                                                    @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.update(eventId, categoryId, promotionId, organizerId, request));
    }

    @DeleteMapping("/events/{eventId}/categories/{categoryId}/promotions/{promotionId}")
    @Operation(summary = "Delete a promotion", description = "Only possible while the promotion has never been used.")
    public ResponseEntity<Void> delete(@PathVariable Long eventId,
                                       @PathVariable Long categoryId,
                                       @PathVariable Long promotionId,
                                       @RequestParam UUID organizerId) {
        promotionService.delete(eventId, categoryId, promotionId, organizerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/categories/{categoryId}/promotions/{promotionId}")
    @Operation(summary = "Get one of my promotions")
    public ResponseEntity<PromotionResponse> getOwnedPromotion(@PathVariable Long eventId,
                                                               @PathVariable Long categoryId,
                                                               @PathVariable Long promotionId,
                                                               @RequestParam UUID organizerId) {
        return ResponseEntity.ok(promotionService.getOwnedPromotion(eventId, categoryId, promotionId, organizerId));
    }

    @GetMapping("/events/{eventId}/categories/{categoryId}/promotions")
    @Operation(summary = "List the promotions of one of my ticket categories")
    public ResponseEntity<List<PromotionResponse>> listByCategory(@PathVariable Long eventId,
                                                                  @PathVariable Long categoryId,
                                                                  @RequestParam UUID organizerId) {
        return ResponseEntity.ok(promotionService.listByCategory(eventId, categoryId, organizerId));
    }

    @GetMapping("/organizers/{organizerId}/promotions")
    @Operation(summary = "List all my promotions")
    public ResponseEntity<List<PromotionResponse>> listByOrganizer(@PathVariable UUID organizerId) {
        return ResponseEntity.ok(promotionService.listByOrganizer(organizerId));
    }
}