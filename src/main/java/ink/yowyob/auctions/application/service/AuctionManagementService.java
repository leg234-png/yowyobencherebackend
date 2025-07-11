//---> PATH: src/main/java/ink/yowyob/auctions/application/service/AuctionManagementService.java
package ink.yowyob.auctions.application.service;

import ink.yowyob.auctions.application.port.in.CreateAuctionUseCase;
import ink.yowyob.auctions.application.port.in.FindAuctionUseCase;
import ink.yowyob.auctions.application.port.out.AuctionRepositoryPort;
import ink.yowyob.auctions.application.port.out.CategoryRepositoryPort;
import ink.yowyob.auctions.application.port.out.ExternalServicePort;
import ink.yowyob.auctions.domain.enumeration.AuctionStatus;
import ink.yowyob.auctions.domain.model.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionManagementService implements CreateAuctionUseCase, FindAuctionUseCase {

    private final AuctionRepositoryPort auctionRepository;
    private final ExternalServicePort externalServicePort;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public Mono<Auction> createAuction(CreateAuctionCommand command) {
        // Validation métier avant la création
        Mono<Boolean> agencyExists = externalServicePort.agencyExists(command.getAgencyId());
        Mono<Boolean> categoryExists = categoryRepository.findById(command.getCategoryId()).hasElement();

        return Mono.zip(agencyExists, categoryExists)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new IllegalArgumentException("Agency with ID " + command.getAgencyId() + " does not exist."));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new IllegalArgumentException("Category with ID " + command.getCategoryId() + " does not exist."));
                    }

                    Auction auction = Auction.builder()
                            .id(UUID.randomUUID())
                            .agencyId(command.getAgencyId())
                            .title(command.getTitle())
                            .description(command.getDescription())
                            .startingPrice(command.getStartingPrice())
                            .currentPrice(command.getStartingPrice()) // Le prix courant est le prix de départ au début
                            .startDate(command.getStartDate())
                            .endDate(command.getEndDate())
                            .status(command.getStartDate().isAfter(LocalDateTime.now()) ? AuctionStatus.SCHEDULED : AuctionStatus.OPEN)
                            .categoryId(command.getCategoryId())
                            .itemCondition(command.getItemCondition())
                            .imageUrls(command.getImageUrls())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return auctionRepository.save(auction);
                });
    }

    @Override
    public Mono<Auction> findAuctionById(UUID auctionId) {
        return auctionRepository.findById(auctionId);
    }

    @Override
    public Flux<Auction> findAuctionsByAgency(UUID agencyId) {
        return auctionRepository.findByAgencyId(agencyId);
    }

    @Override
    public Flux<Auction> findAuctionsByCategory(UUID categoryId) {
        return auctionRepository.findByCategoryId(categoryId);
    }
}
