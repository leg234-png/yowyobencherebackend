package com.yowyob.dev.controllers;

import com.yowyob.dev.exceptions.BadRequestException;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import com.yowyob.dev.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public PaymentController(AuctionRepository auctionRepository, BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    @PostMapping("/auction/{auctionId}")
    @Operation(summary = "Effectuer le paiement pour une enchère gagnée")
    public Mono<ResponseEntity<String>> payForAuction(
            @Parameter(description = "ID de l'enchère à payer") @PathVariable UUID auctionId) {

        Mono<Auction> auctionMono = auctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new BadRequestException("Auction not found")));

        Mono<String> winnerUsernameMono = bidRepository.findByAuctionId(auctionId)
                .sort(Comparator.comparing(bid -> bid.getPrice(), Comparator.reverseOrder()))
                .next()
                .map(bid -> bid.getUsername())
                .switchIfEmpty(Mono.error(new BadRequestException("No winner found for this auction")));

        Mono<String> currentUserMono = JwtUtils.getCurrentUsername();

        return Mono.zip(auctionMono, winnerUsernameMono, currentUserMono)
                .flatMap(tuple -> {
                    Auction auction = tuple.getT1();
                    String winner = tuple.getT2();
                    String currentUser = tuple.getT3();

                    if (!currentUser.equals(winner)) {
                        return Mono.error(new BadRequestException("Only the winner can pay for this auction."));
                    }
                    if (auction.getStatus() != com.yowyob.dev.enumeration.AuctionStatus.CLOSE) {
                        return Mono.error(new BadRequestException("Auction is not closed yet."));
                    }
                    if ("PAID".equals(auction.getPaymentStatus())) {
                        return Mono.error(new BadRequestException("This auction has already been paid for."));
                    }

                    auction.setPaymentStatus("PAID");
                    auction.setUpdatedAt(LocalDateTime.now());
                    return auctionRepository.save(auction)
                            .map(savedAuction -> ResponseEntity.ok("Payment successful for auction: " + savedAuction.getTitle()));
                });
    }
}