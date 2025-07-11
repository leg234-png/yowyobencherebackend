//---> PATH: src/main/java/ink/yowyob/auctions/application/port/in/FindAuctionUseCase.java
package ink.yowyob.auctions.application.port.in;

import ink.yowyob.auctions.domain.model.Auction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FindAuctionUseCase {
    Mono<Auction> findAuctionById(UUID auctionId);
    Flux<Auction> findAuctionsByAgency(UUID agencyId);
    Flux<Auction> findAuctionsByCategory(UUID categoryId);
}
