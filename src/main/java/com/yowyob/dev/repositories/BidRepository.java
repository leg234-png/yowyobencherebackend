//---> PATH: src/main/java/com/yowyob/dev/repositories/BidRepository.java
package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Bid;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface BidRepository extends R2dbcRepository<Bid, UUID> {
    Flux<Bid> findByAuctionId(UUID auctionId);

    @Query("SELECT DISTINCT username FROM bid WHERE auction_id = :auctionId ORDER BY price DESC")
    Flux<String> findParticipantsByAuctionOrderByPriceDesc(UUID auctionId);
}