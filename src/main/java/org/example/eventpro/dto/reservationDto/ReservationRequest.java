package org.example.eventpro.dto.reservationDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationRequest {

    @NotEmpty(message = "La réservation doit contenir au moins une ligne")
    @Valid
    private List<ReservationLineRequest> lines;
}