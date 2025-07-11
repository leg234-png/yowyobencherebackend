//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/controller/AuctionController.java
package ink.yowyob.auctions.presentation.rest.controller;

import ink.yowyob.auctions.application.port.in.FindAuctionUseCase;
import ink.yowyob.auctions.application.port.in.PlaceBidUseCase;
import ink.yowyob.auctions.presentation.rest.dto.AuctionResponse;
import ink.yowyob.auctions.presentation.rest.dto.BidRequest;
import ink.yowyob.auctions.presentation.rest.dto.BidResponse;
import ink.yowyob.auctions.presentation.rest.mapper.AuctionRestMapper;
import ink.yowyob.auctions.presentation.rest.mapper.BidRestMapper;
import ink.yowyob.auctions.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final FindAuctionUseCase findAuctionUseCase;
    private final PlaceBidUseCase placeBidUseCase;
    private final AuctionRestMapper auctionMapper;
    private final BidRestMapper bidMapper;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuctionResponse> getAuctionById(@PathVariable UUID id) {
        return findAuctionUseCase.findAuctionById(id)
                .map(auctionMapper::toResponse);
    }

    @PostMapping("/{id}/bids")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()") // Seuls les utilisateurs connectés peuvent enchérir
    public Mono<BidResponse> placeBidOnAuction(@PathVariable("id") UUID auctionId,
                                               @Valid @RequestBody BidRequest bidRequest) {
        return JwtUtils.getCurrentUsername()
                .flatMap(username -> {
                    var command = bidMapper.toCommand(bidRequest, auctionId, username);
                    return placeBidUseCase.placeBid(command);
                })
                .map(bidMapper::toResponse);
    }
}