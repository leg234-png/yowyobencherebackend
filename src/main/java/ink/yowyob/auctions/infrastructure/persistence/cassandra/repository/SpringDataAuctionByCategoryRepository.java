package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionByCategoryEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SpringDataAuctionByCategoryRepository extends ReactiveCassandraRepository<AuctionByCategoryEntity, AuctionByCategoryEntity.Key> {
    Flux<AuctionByCategoryEntity> findByKeyCategoryId(UUID categoryId);

}
