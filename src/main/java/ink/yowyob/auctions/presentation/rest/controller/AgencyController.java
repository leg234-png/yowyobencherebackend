//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/controller/AgencyController.java
package ink.yowyob.auctions.presentation.rest.controller;

import ink.yowyob.auctions.application.port.in.CreateAuctionUseCase;
import ink.yowyob.auctions.application.port.in.FindAuctionUseCase;
import ink.yowyob.auctions.presentation.rest.dto.AuctionRequest;
import ink.yowyob.auctions.presentation.rest.dto.AuctionResponse;
import ink.yowyob.auctions.presentation.rest.mapper.AuctionRestMapper;
import ink.yowyob.auctions.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/agencies/{agencyId}/auctions")
@RequiredArgsConstructor
public class AgencyController {

    private final CreateAuctionUseCase createAuctionUseCase;
    private final FindAuctionUseCase findAuctionUseCase;
    @Qualifier("auctionRestMapper")
    private final AuctionRestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()") // Seuls les utilisateurs authentifiés peuvent créer
    public Mono<AuctionResponse> createAuctionForAgency(@PathVariable UUID agencyId,
                                                        @Valid @RequestBody AuctionRequest request,
                                                        ServerWebExchange exchange) {
        var command = mapper.toCommand(request, agencyId);
        return createAuctionUseCase.createAuction(command).map(mapper::toResponse);

    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<AuctionResponse> getAuctionsByAgency(@PathVariable UUID agencyId) {
        return findAuctionUseCase.findAuctionsByAgency(agencyId)
                .map(mapper::toResponse);
    }
}