package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OffreDTO {

    @NotNull(message = "Le montant de l'offre est obligatoire.")
    @Positive(message = "Le montant doit être supérieur à zéro.")
    private Double montant;

    @NotNull(message = "L'identifiant de l'enchère est obligatoire.")
    private UUID enchereId;

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire.")
    private UUID utilisateurId;
}
