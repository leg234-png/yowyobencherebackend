package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class BidDTO {

    @NotNull(message = "Le montant de l'offre est obligatoire.")
    @Positive(message = "Le montant doit être supérieur à zéro.")
    private Double price;

    @NotNull(message = "L'identifiant de l'enchère est obligatoire.")
    private UUID auctionId;

    @NotNull(message = "L'username de l'utilisateur est obligatoire.")
    private String username;
}
