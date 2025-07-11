//---> PATH: src/main/java/ink/yowyob/auctions/domain/model/Auction.java
package ink.yowyob.auctions.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import ink.yowyob.auctions.domain.enumeration.AuctionStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Auction {
    private final UUID id;
    private String title;
    private String description;
    private final BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private final LocalDateTime startDate;
    private LocalDateTime endDate;
    private AuctionStatus status;
    private final UUID agencyId;
    private UUID categoryId;
    private String itemCondition;
    private Set<String> imageUrls;
    private Set<String> participants; // Ensemble des usernames des enchérisseurs
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}