//---> PATH: src/main/java/ink/yowyob/auctions/domain/model/Bid.java
package ink.yowyob.auctions.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Bid {
    private final UUID id;
    private final UUID auctionId;
    private final String username; // L'utilisateur qui a fait l'offre
    private final BigDecimal price;
    private final LocalDateTime createdAt;
}