package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EnchereDTO {

    @NotBlank(message = "Le titre est obligatoire.")
    private String titre;

    @NotBlank(message = "La description est obligatoire.")
    private String description;

    @NotNull(message = "Le prix initial est obligatoire.")
    @Positive(message = "Le prix initial doit être supérieur à zéro.")
    private Double prixInitial;

    @NotNull(message = "La date de début est obligatoire.")
    @Future(message = "La date de début doit être dans le futur.")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire.")
    @Future(message = "La date de fin doit être dans le futur.")
    private LocalDateTime dateFin;

    @NotNull(message = "Le vendeur est obligatoire.")
    private UUID vendeurId;

    private UUID categorieId;

    private String imageUrl;

    private String condition;
}
