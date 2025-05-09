package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AuctionDTO {

    @NotBlank(message = "Le titre est obligatoire.")
    private String title;

    @NotBlank(message = "La description est obligatoire.")
    private String description;

    @NotNull(message = "Le prix initial est obligatoire.")
    @Positive(message = "Le prix initial doit être supérieur à zéro.")
    private Double startingPrice;

    @NotNull(message = "La date de début est obligatoire.")
    @Future(message = "La date de début doit être dans le futur.")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire.")
    @Future(message = "La date de fin doit être dans le futur.")
    private LocalDateTime endDate;

    @NotNull(message = "Le vendeur est obligatoire.")
    private String sellerUsername;

    private UUID categoryId;

    private String imageUrl;

    private String ItemCondition;
}
