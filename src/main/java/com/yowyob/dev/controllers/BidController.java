package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.BidDTO;
import com.yowyob.dev.dto.responseDTO.UserDTO;
import com.yowyob.dev.models.Bid;
import com.yowyob.dev.services.BidService;
import com.yowyob.dev.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;


import java.util.UUID;

@RestController
@RequestMapping("/bids") // Renommé pour suivre les conventions REST (pluriel)
@Slf4j
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Bid> createBid(@Valid @RequestBody BidDTO dto) {
        return JwtUtils.getCurrentUserInfo()
                .flatMap(userInfo -> {
                    // S'assurer que l'utilisateur fait une offre pour lui-même
                    dto.setUsername(userInfo.getUsername());
                    return bidService.createBid(dto);
                });
    }

    @GetMapping("/{id}")
    public Mono<Bid> getBid(@PathVariable UUID id) {
        return bidService.getBid(id);
    }

    @GetMapping
    public Flux<Bid> getAllBids() {
        return bidService.getAllBids();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBid(@PathVariable UUID id) {
        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} deleting bid {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> bidService.deleteBid(id));
    }

    // Endpoint public - voir les participants d'une enchère
    @GetMapping("/auction/{auctionId}/participants")
    public Flux<UserDTO> getAuctionParticipants(@PathVariable UUID auctionId) {
        return bidService.getParticipants(auctionId);
    }

    // Endpoint public - voir toutes les offres d'une enchère
    @GetMapping("/auction/{auctionId}")
    public Flux<Bid> getAllBidsForAuction(@PathVariable UUID auctionId) {
        return bidService.getAllBidsOfAuction(auctionId);
    }

    // Endpoint protégé - voir ses propres offres
    @GetMapping("/my-bids")
    public Flux<Bid> getMyBids() {
        return JwtUtils.getCurrentUsername()
                .flatMapMany(username ->
                        bidService.getAllBids()
                                .filter(bid -> username.equals(bid.getUsername()))
                );
    }
}