//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/SpringDataBidRepository.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.BidEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SpringDataBidRepository extends ReactiveCassandraRepository<BidEntity, BidEntity.Key> {
    Flux<BidEntity> findByKeyAuctionId(UUID auctionId);
}