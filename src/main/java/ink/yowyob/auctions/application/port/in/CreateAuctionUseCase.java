package ink.yowyob.auctions.application.port.in;

import ink.yowyob.auctions.domain.model.Auction;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface CreateAuctionUseCase {

    Mono<Auction> createAuction(CreateAuctionCommand command);

    @Value
    @Builder
    class CreateAuctionCommand {
        @NotNull UUID agencyId;
        @NotNull String title;
        @NotNull String description;
        @NotNull BigDecimal startingPrice;
        @NotNull LocalDateTime startDate;
        @NotNull LocalDateTime endDate;
        @NotNull UUID categoryId;
        String itemCondition;
        Set<String> imageUrls; // Les URLs sont déjà fournies, la logique d'upload se fait avant
    }
}
