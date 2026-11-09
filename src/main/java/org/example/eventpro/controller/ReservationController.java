package org.example.eventpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.reservationDto.ReservationRequest;
import org.example.eventpro.dto.reservationDto.ReservationResponse;
import org.example.eventpro.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Create and manage the reservations of a client")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation",
            description = "Reserves the places of every line for 10 minutes. Stock, quota and promo codes are checked atomically.")
    public ResponseEntity<ReservationResponse> create(@PathVariable UUID clientId,
                                                      @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.create(clientId, request);
        URI location = URI.create("/api/clients/" + clientId + "/reservations/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get one of my reservations")
    public ResponseEntity<ReservationResponse> getOwned(@PathVariable UUID clientId,
                                                        @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getOwned(reservationId, clientId));
    }

    @GetMapping
    @Operation(summary = "List my reservations", description = "Most recent first.")
    public ResponseEntity<List<ReservationResponse>> listByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(reservationService.listByClient(clientId));
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancel a pending reservation",
            description = "Only possible while the reservation is PENDING_PAYMENT. Releases the places and the promo usage.")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable UUID clientId,
                                                      @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancel(reservationId, clientId));
    }
}