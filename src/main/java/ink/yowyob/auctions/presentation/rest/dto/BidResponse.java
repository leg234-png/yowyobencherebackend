//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/dto/BidResponse.java
package ink.yowyob.auctions.presentation.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BidResponse {
    private UUID id;
    private UUID auctionId;
    private String username;
    private BigDecimal price;
    private LocalDateTime createdAt;
}