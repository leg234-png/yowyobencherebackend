package com.yowyob.dev.dto.responseDTO;

import com.yowyob.dev.enumeration.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private Double startingPrice;
    private Double currentPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private AuctionStatus status;
    private UUID agencyId;
    private CategoryDTO category; // Utiliser un DTO pour la catégorie
    private List<String> imageUrls;
    private String itemCondition;
    private List<String> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}