package org.example.eventpro.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateOrganizerRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
    private String email;

    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(max = 150, message = "Le nom de l'organisation ne doit pas dépasser 150 caractères")
    private String organizationName;
}
