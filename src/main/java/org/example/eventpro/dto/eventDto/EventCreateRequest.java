package org.example.eventpro.dto.eventDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventCreateRequest {
    @NotNull(message = "L'identifiant de l'organisateur est obligatoire")
    private UUID organizerId;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255)
    private String address;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    private LocalDateTime saleOpeningDate;
    private LocalDateTime saleClosingDate;

    @NotEmpty(message = "Au moins une catégorie de billets est obligatoire")
    @Valid
    private List<TicketCategoryRequest> categories;
}
