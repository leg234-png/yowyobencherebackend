//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/dto/AuctionResponse.java
package ink.yowyob.auctions.presentation.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import ink.yowyob.auctions.domain.enumeration.AuctionStatus;

@Data
@Builder
public class AuctionResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private AuctionStatus status;
    private UUID agencyId;
    private CategoryResponse category;
    private String itemCondition;
    private Set<String> imageUrls;
    private Set<String> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}