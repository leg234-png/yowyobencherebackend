//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/SpringDataAuctionRepository.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAuctionRepository extends ReactiveCassandraRepository<AuctionEntity, UUID> {
}
