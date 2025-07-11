//---> PATH: src/main/java/ink/yowyob/auctions/application/port/in/PlaceBidUseCase.java
package ink.yowyob.auctions.application.port.in;

import ink.yowyob.auctions.domain.model.Bid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface PlaceBidUseCase {

    Mono<Bid> placeBid(PlaceBidCommand command);

    @Value
    @Builder
    class PlaceBidCommand {
        @NotNull UUID auctionId;
        @NotNull String bidderUsername; // L'utilisateur qui fait l'offre
        @NotNull BigDecimal price;
    }
}
