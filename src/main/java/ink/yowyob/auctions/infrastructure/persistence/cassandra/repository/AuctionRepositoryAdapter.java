//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/AuctionRepositoryAdapter.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;



import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.insertInto;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;

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
    private static final DateTimeFormatter BUCKET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    public Mono<Void> updateAndCleanupLookups(Auction auction) {
        log.debug("Cleaning up lookups for closed auction {}", auction.getId());

        var auctionEntity = mapper.toEntity(auction);

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);

        // 1. Mettre à jour l'entité principale avec le nouveau statut

        Insert insert = insertInto("auction")
                .value("status", literal(auction.getStatus()));

        SimpleStatement statement = insert.build();

        batch.addStatement(statement);



        // 2. Supprimer les entrées des tables de lookup
        // On a besoin de la date de fin originale pour construire la clé de suppression
        // Note: l'objet `auction` a toujours la bonne date de fin.
        String bucket = auction.getEndDate().format(BUCKET_FORMATTER);

        // Suppression de la table de clôture
        String deleteFromEndDateLookupCql = String.format(
                "DELETE FROM open_auctions_by_end_date WHERE end_date_bucket = '%s' AND end_date = '%s' AND auction_id = %s",
                bucket, auction.getEndDate().toString(), auction.getId()
        );
        batch.addStatement(SimpleStatement.newInstance(deleteFromEndDateLookupCql));

        // Optionnel : Si vous ne voulez plus que les enchères fermées apparaissent
        // dans les listes "par agence" ou "par catégorie", vous pouvez aussi les supprimer.
        // Sinon, vous pouvez les laisser et filtrer côté client ou API par statut.
        // Pour une vraie propreté, supprimons-les.

        // Suppression de la table par agence
        String deleteFromAgencyLookupCql = String.format(
                "DELETE FROM auctions_by_agency WHERE agency_id = %s AND end_date = '%s' AND auction_id = %s",
                auction.getAgencyId(), auction.getEndDate().toString(), auction.getId()
        );
        batch.addStatement(SimpleStatement.newInstance(deleteFromAgencyLookupCql));

        // Suppression de la table par catégorie
        if (auction.getCategoryId() != null) {
            String deleteFromCategoryLookupCql = String.format(
                    "DELETE FROM auctions_by_category WHERE category_id = %s AND end_date = '%s' AND auction_id = %s",
                    auction.getCategoryId(), auction.getEndDate().toString(), auction.getId()
            );
            batch.addStatement(SimpleStatement.newInstance(deleteFromCategoryLookupCql));
        }

        return cassandraTemplate.getReactiveCqlOperations().execute(batch.build()).then();
    }


    @Override
    public Flux<Auction> findOpenAuctionsEndingBefore(LocalDateTime dateTime) {
        String todayBucket = dateTime.format(BUCKET_FORMATTER);
        String yesterdayBucket = dateTime.minusDays(1).format(BUCKET_FORMATTER);

        String cqlToday = "SELECT auction_id FROM open_auctions_by_end_date WHERE end_date_bucket = '" + todayBucket + "' AND end_date < '" + dateTime.toString() + "'";
        String cqlYesterday = "SELECT auction_id FROM open_auctions_by_end_date WHERE end_date_bucket = '" + yesterdayBucket + "'";

        Flux<UUID> idsFromToday = cassandraTemplate.getReactiveCqlOperations().queryForRows(cqlToday)
                .mapNotNull(row -> row.getUuid("auction_id"));
        Flux<UUID> idsFromYesterday = cassandraTemplate.getReactiveCqlOperations().queryForRows(cqlYesterday)
                .mapNotNull(row -> row.getUuid("auction_id"));

        return Flux.concat(idsFromToday, idsFromYesterday)
                .distinct()
                .flatMap(this::findById);
    }
}