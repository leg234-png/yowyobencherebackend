//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/SpringDataAuctionByAgencyRepository.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionByAgencyEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SpringDataAuctionByAgencyRepository extends ReactiveCassandraRepository<AuctionByAgencyEntity, AuctionByAgencyEntity.Key> {
    Flux<AuctionByAgencyEntity> findByKeyAgencyId(UUID agencyId);
}