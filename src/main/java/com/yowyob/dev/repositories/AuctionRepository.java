package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Auction;
import com.yowyob.dev.enumeration.AuctionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
// On utilise R2dbcRepository ou ReactiveCrudRepository
public interface AuctionRepository extends R2dbcRepository<Auction, UUID> {

    Flux<Auction> findByStatus(AuctionStatus status);
    Flux<Auction> findByCategoryId(UUID categoryId);
    Mono<Long> countByStatus(AuctionStatus status);
    Mono<Long> countByCategoryId(UUID categoryId);

    Flux<Auction> findByEndDateBeforeAndStatus(LocalDateTime now, AuctionStatus auctionStatus);
    Flux<Auction> findByAgencyId(UUID agencyId);

    // Les requêtes avec pagination sont gérées différemment
    @Query("SELECT * FROM auction WHERE created_at >= :cutoffDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Auction> findRecentAuctions(LocalDateTime cutoffDate, int limit, long offset);

    @Query("SELECT COUNT(id) FROM auction WHERE created_at >= :cutoffDate")
    Mono<Long> countRecentAuctions(LocalDateTime cutoffDate);

    @Query("SELECT * FROM auction WHERE status = :status AND end_date BETWEEN :startTime AND :endTime ORDER BY end_date ASC LIMIT :limit OFFSET :offset")
    Flux<Auction> findEndingSoonAuctions(AuctionStatus status, LocalDateTime startTime, LocalDateTime endTime, int limit, long offset);

    @Query("SELECT * FROM auction ORDER BY end_date ASC LIMIT :limit OFFSET :offset")
    Flux<Auction> findAllAuctionsPaged(int limit, long offset);


    @Query("SELECT COUNT(id) FROM auction WHERE status = :status AND end_date BETWEEN :startTime AND :endTime")
    Mono<Long> countEndingSoonAuctions(AuctionStatus status, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(*) FROM auction")
    Mono<Long> countAllAuctions();

    @Query("SELECT DISTINCT a.* FROM auction a JOIN bid b ON a.id = b.auction_id WHERE b.username = :username")
    Flux<Auction> findAuctionsByParticipantUsername(String username);

    @Query("""
        SELECT a.* FROM auction a
        JOIN (
            -- Trouve le prix maximum pour chaque enchère terminée
            SELECT auction_id, MAX(price) as max_price
            FROM bid
            GROUP BY auction_id
        ) b_max ON a.id = b_max.auction_id
        -- Jointure avec la table des offres pour trouver le gagnant
        JOIN bid b_winner ON b_winner.auction_id = a.id AND b_winner.price = b_max.max_price
        WHERE a.status = 'CLOSE' AND b_winner.username = :username
    """)
    Flux<Auction> findWonAuctionsByUsername(String username);


    Mono<Long> countByAgencyId(UUID agencyId);
}