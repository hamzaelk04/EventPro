package org.example.eventpro.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Pattern(regexp = "^(\\+212|0)[5-7]\\d{8}$",
            message = "Numéro de téléphone marocain invalide")
    private String phone;

    @Size(max = 150, message = "Le nom de l'organisation ne doit pas dépasser 150 caractères")
    private String organizationName;
}