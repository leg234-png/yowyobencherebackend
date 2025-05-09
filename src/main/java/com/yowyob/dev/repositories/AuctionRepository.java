package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Auction;
import com.yowyob.dev.enumeration.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    List<Auction> findBySellerUsername(String vendeurUsername);

    @Query("SELECT e FROM Auction e WHERE :username IN elements(e.participants)")
    List<Auction> findByParticipant(String username);

    List<Auction> findByStatus(AuctionStatus statut);
    List<Auction> findByCategory_Id(UUID categorieId);

    List<Auction> findByEndDateBeforeAndStatus(LocalDateTime now, AuctionStatus auctionStatus);
}
