package com.yowyob.dev.services;

import com.yowyob.dev.dto.responseDTO.DashboardStatsDTO;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple6;

import java.util.UUID;

@Service
@Slf4j
public class DashboardService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public DashboardService(AuctionRepository auctionRepository, BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    public Mono<DashboardStatsDTO> getDashboardStatistics(String username, UUID agencyId) {
        log.info("Generating dashboard statistics for user {}", username);

        Mono<Long> totalActiveAuctionsMono = auctionRepository.countByStatus(AuctionStatus.OPEN);
        Mono<Long> totalClosedAuctionsMono = auctionRepository.countByStatus(AuctionStatus.CLOSE);
        Mono<Long> totalBidsMono = bidRepository.count();

        Mono<Long> myCreatedAuctionsMono = auctionRepository.countByAgencyId(agencyId);
        Mono<Long> myParticipationsMono = auctionRepository.findAuctionsByParticipantUsername(username).count();
        Mono<Long> myWonAuctionsMono = auctionRepository.findWonAuctionsByUsername(username).count();

        return Mono.zip(
                totalActiveAuctionsMono,
                totalClosedAuctionsMono,
                totalBidsMono,
                myCreatedAuctionsMono,
                myParticipationsMono,
                myWonAuctionsMono
        ).map(this::buildDTOFromTuple);
    }

    private DashboardStatsDTO buildDTOFromTuple(Tuple6<Long, Long, Long, Long, Long, Long> tuple) {
        return DashboardStatsDTO.builder()
                .totalActiveAuctions(tuple.getT1())
                .totalClosedAuctions(tuple.getT2())
                .totalBidsPlaced(tuple.getT3())
                .myCreatedAuctions(tuple.getT4())
                .myParticipationsCount(tuple.getT5())
                .myWonAuctionsCount(tuple.getT6())
                .build();
    }
}