package org.example.eventpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryResponse;
import org.example.eventpro.service.TicketCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Ticket categories", description = "Manage the ticket categories of an event")
public class TicketCategoryController {

    private final TicketCategoryService categoryService;

    //    CREATE
    @PostMapping("/events/{eventId}/categories")
    @Operation(summary = "Add a ticket category to an event",
            description = "allowed while the event is DRAFT, or PUBLISHED and not started yet. the name must be unique within the event.")
    public ResponseEntity<TicketCategoryResponse> add(@PathVariable Long eventId,
                                                      @RequestParam UUID organizerId,
                                                      @Valid @RequestBody TicketCategoryRequest request) {

        TicketCategoryResponse response = categoryService.add(eventId, organizerId, request);

        URI location = URI.create("api/events/" + eventId + "/categories" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    //    UPDATE
    @PutMapping("/events/{eventId}/categories/{categoryId}")
    @Operation(summary = "Update a ticket category",
            description = "Full replacement: a null quotaMaxPerClient removes the quota. The total quantity cannot go below the places already reserved or sold.")
    public ResponseEntity<TicketCategoryResponse> update(@PathVariable Long eventId,
                                                         @PathVariable Long categoryId,
                                                         @RequestParam UUID organizerId,
                                                         @Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(eventId, categoryId, organizerId, request));
    }

    //    DELETE
    @DeleteMapping("/events/{eventId}/categories/{categoryId}")
    @Operation(summary = "Delete a ticket category",
            description = "Only possible while the event is DRAFT.")
    public ResponseEntity<Void> delete(@PathVariable Long eventId,
                                       @PathVariable Long categoryId,
                                       @RequestParam UUID organizerId) {
        categoryService.delete(eventId, categoryId, organizerId);
        return ResponseEntity.noContent().build();
    }

    //    List of public event
    @GetMapping("/events/{eventId}/categories")
    @Operation(summary = "List the ticket categories of a public event",
            description = "Draft events are hidden (404).")
    public ResponseEntity<List<TicketCategoryResponse>> listByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(categoryService.listByEvent(eventId));
    }

    //   LIST OF ONE EVENT
    @GetMapping("/organizers/{organizerId}/events/{eventId}/categories")
    @Operation(summary = "List the ticket categories of one of my events",
            description = "Owner view, drafts included.")
    public ResponseEntity<List<TicketCategoryResponse>> listOwnedByEvent(@PathVariable UUID organizerId,
                                                                         @PathVariable Long eventId) {
        return ResponseEntity.ok(categoryService.listOwnedByEvent(eventId, organizerId));
    }

    //  GET ONE TICKET OF ONE EVENT
    @GetMapping("/organizers/{organizerId}/events/{eventId}/categories/{categoryId}")
    @Operation(summary = "Get one ticket category of one of my events",
            description = "Owner view, drafts included.")
    public ResponseEntity<TicketCategoryResponse> getOwnedCategory(@PathVariable UUID organizerId,
                                                                   @PathVariable Long eventId,
                                                                   @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getOwnedCategory(eventId, categoryId, organizerId));
    }
}
