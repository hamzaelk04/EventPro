package org.example.eventpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.eventDto.*;
import org.example.eventpro.dto.userDto.PageResponse;
import org.example.eventpro.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Gestion des événements et de leurs catégories de billets")
public class EventController {
    private final EventService eventService;

    //    CREATE
    @PostMapping("/events")
    @Operation(summary = "Créer un événement (brouillon) avec ses catégories de billets")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest request) {
        EventResponse body = eventService.create(request);

        return ResponseEntity.created(URI.create("/api/events" + body.getId())).body(body);
    }

    //    PUBLIC READS
    @GetMapping("/events")
    @Operation(summary = "Catalogue des événements publiés à venir, avec les places restantes")
    public ResponseEntity<PageResponse<EventSummaryResponse>> catalogue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(eventService.listCatalogue(page, size));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Détail d'un événement (les brouillons sont masqués)")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getPublicEvent(id));
    }

    // ---------- organizer actions ----------

    @PutMapping("/events/{id}")
    @Operation(summary = "Modifier un événement (dates figées après publication)")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id,
            @RequestParam UUID organizerId,
            @Valid @RequestBody EventUpdateRequest request) {
        return ResponseEntity.ok(eventService.update(id, organizerId, request));
    }

    @PutMapping("/events/{id}/publish")
    @Operation(summary = "Publier un événement (DRAFT -> PUBLISHED)")
    public ResponseEntity<EventResponse> publish(
            @PathVariable Long id,
            @RequestParam UUID organizerId) {
        return ResponseEntity.ok(eventService.publish(id, organizerId));
    }

    @PutMapping("/events/{id}/cancel")
    @Operation(summary = "Annuler un événement")
    public ResponseEntity<EventResponse> cancel(
            @PathVariable Long id,
            @RequestParam UUID organizerId) {
        return ResponseEntity.ok(eventService.cancel(id, organizerId));
    }

    @GetMapping("/events/{id}/sales")
    @Operation(summary = "Billets vendus pour un événement (US7)")
    public ResponseEntity<EventSalesResponse> sales(
            @PathVariable Long id,
            @RequestParam UUID organizerId) {
        return ResponseEntity.ok(eventService.getSales(id, organizerId));
    }

    // ---------- organizer's own events ----------

    @GetMapping("/organizers/{organizerId}/events")
    @Operation(summary = "Lister les événements d'un organisateur, tous statuts")
    public ResponseEntity<PageResponse<EventSummaryResponse>> listByOrganizer(
            @PathVariable UUID organizerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(eventService.listByOrganizer(organizerId, page, size));
    }

    @GetMapping("/organizers/{organizerId}/events/{eventId}")
    @Operation(summary = "Détail d'un événement de l'organisateur, brouillons inclus")
    public ResponseEntity<EventResponse> getOwnedEvent(
            @PathVariable UUID organizerId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getOwnedEvent(eventId, organizerId));
    }
}
