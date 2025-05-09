package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByAuctionId(UUID auctionId);

    @Query("SELECT o.username FROM Bid o WHERE o.auction.id = :auctionId ORDER BY o.price DESC")
    List<String> findParticipantsByAuctionOrderByPriceAsc(@Param("auctionId") UUID auctionId);

}
