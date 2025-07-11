//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/dto/AuctionRequest.java
package ink.yowyob.auctions.presentation.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class AuctionRequest {

    @NotBlank(message = "Title is mandatory.")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters.")
    private String title;

    @NotBlank(message = "Description is mandatory.")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters.")
    private String description;

    @NotNull(message = "Starting price is mandatory.")
    @Positive(message = "Starting price must be positive.")
    private BigDecimal startingPrice;

    @NotNull(message = "Start date is mandatory.")
    @FutureOrPresent(message = "Start date must be in the present or future.")
    private LocalDateTime startDate;

    @NotNull(message = "End date is mandatory.")
    @Future(message = "End date must be in the future.")
    private LocalDateTime endDate;

    @NotNull(message = "Category ID is mandatory.")
    private UUID categoryId;

    private String itemCondition;

    // L'upload d'images se fera via un endpoint séparé pour obtenir les URLs
    private Set<String> imageUrls;
}