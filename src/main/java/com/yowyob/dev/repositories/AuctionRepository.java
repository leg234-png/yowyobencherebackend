package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Auction;
import com.yowyob.dev.enumeration.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {

    @Query("SELECT e FROM Auction e WHERE :username IN elements(e.participants)")
    List<Auction> findByParticipant(String username);

    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findByCategory_Id(UUID categoryId);

    List<Auction> findByEndDateBeforeAndStatus(LocalDateTime now, AuctionStatus auctionStatus);

    List<Auction> findByAgencyId(UUID agencyId);

    /**
     * Trouve les enchères créées après une date donnée, triées par date de création décroissante
     * @param cutoffDate Date limite
     * @param pageable Paramètres de pagination
     * @return Page d'enchères récentes
     */
    @Query("SELECT a FROM Auction a WHERE a.createdAt >= :cutoffDate ORDER BY a.createdAt DESC")
    Page<Auction> findByCreatedAtAfterOrderByCreatedAtDesc(
            @Param("cutoffDate") LocalDateTime cutoffDate,
            Pageable pageable
    );

    /**
     * Trouve les enchères avec un statut donné qui se terminent dans une plage de temps donnée
     * @param status Statut de l'enchère
     * @param startTime Heure de début de la plage
     * @param endTime Heure de fin de la plage
     * @param pageable Paramètres de pagination
     * @return Page d'enchères se terminant bientôt
     */
    @Query("SELECT a FROM Auction a WHERE a.status = :status AND a.endDate BETWEEN :startTime AND :endTime ORDER BY a.endDate ASC")
    Page<Auction> findByStatusAndEndDateBetweenOrderByEndDateAsc(
            @Param("status") AuctionStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

}
