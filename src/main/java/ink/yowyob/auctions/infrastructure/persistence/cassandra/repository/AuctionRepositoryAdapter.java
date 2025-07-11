//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/AuctionRepositoryAdapter.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.application.port.out.AuctionRepositoryPort;
import ink.yowyob.auctions.domain.model.Auction;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.mapper.AuctionCassandraMapper;
import ink.yowyob.auctions.utils.CassandraStatementBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRepositoryAdapter implements AuctionRepositoryPort {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final SpringDataAuctionRepository auctionRepository;
    private final SpringDataAuctionByAgencyRepository auctionByAgencyRepository;
    private final SpringDataAuctionByCategoryRepository auctionByCategoryRepository;
    @Qualifier("auctionCassandraMapper")
    private final AuctionCassandraMapper mapper;

    @Override
    public Mono<Auction> save(Auction auction) {
        var auctionEntity = mapper.toEntity(auction);
        var byAgencyEntity = mapper.toByAgencyEntity(auction);
        var byCategoryEntity = mapper.toByCategoryEntity(auction);

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);

        batch.addStatement(CassandraStatementBuilder.buildInsertStatement(auctionEntity, "auction"));
        batch.addStatement(CassandraStatementBuilder.buildInsertStatement(byAgencyEntity, "auction_by_agency"));
        batch.addStatement(CassandraStatementBuilder.buildInsertStatement(byCategoryEntity, "auction_by_category"));

        return cassandraTemplate.getReactiveCqlOperations()
                .execute(batch.build())
                .doOnSuccess(v -> log.info("Successfully saved auction {} and its lookups.", auction.getId()))
                .doOnError(e -> log.error("Failed to save auction {} in a batch.", auction.getId(), e))
                .thenReturn(auction);

    }
    
    @Override
    public Mono<Void> update(Auction auction) {
        // La mise à jour est similaire, mais utilise des requêtes UPDATE ou INSERT (upsert)
        // Pour la simplicité, nous réutilisons la logique de `save`.
        // Une implémentation plus fine pourrait mettre à jour uniquement les champs modifiés.
        return save(auction).then();
    }

    @Override
    public Mono<Auction> findById(UUID auctionId) {
        return auctionRepository.findById(auctionId)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Auction> findByAgencyId(UUID agencyId) {
        return auctionByAgencyRepository.findByKeyAgencyId(agencyId)
                .flatMap(lookup -> findById(lookup.getKey().getAuctionId()));
    }

    @Override
    public Flux<Auction> findByCategoryId(UUID categoryId) {
        return auctionByCategoryRepository.findByKeyCategoryId(categoryId)
                .flatMap(lookup -> findById(lookup.getKey().getAuctionId()));
    }

    @Override
    public Flux<Auction> findOpenAuctionsEndingBefore(LocalDateTime now) {
        return null;
    }
}