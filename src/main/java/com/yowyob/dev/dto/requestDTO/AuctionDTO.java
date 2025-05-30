package com.yowyob.dev.dto.requestDTO;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

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
    @FutureOrPresent(message = "La date de début doit être dans le futur ou dans le présent.")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire.")
    @FutureOrPresent(message = "La date de fin doit être dans le futur ou  dans le présent.")
    private LocalDateTime endDate;

    @NotNull(message = "Le vendeur est obligatoire.")
    private UUID agencyId;

    private UUID categoryId;

    @NotNull(message = "Les images sont obligatoires.")
    private List<MultipartFile> images;

    private String ItemCondition;
}
