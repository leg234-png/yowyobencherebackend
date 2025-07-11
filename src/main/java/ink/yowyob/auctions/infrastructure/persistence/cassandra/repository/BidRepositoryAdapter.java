//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/repository/BidRepositoryAdapter.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.application.port.out.BidRepositoryPort;
import ink.yowyob.auctions.domain.model.Bid;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.mapper.BidCassandraMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BidRepositoryAdapter implements BidRepositoryPort {

    private final SpringDataBidRepository repository;
    private final BidCassandraMapper mapper;

    @Override
    public Mono<Bid> save(Bid bid) {
        return repository.save(mapper.toEntity(bid))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Bid> findByAuctionId(UUID auctionId) {
        return repository.findByKeyAuctionId(auctionId)
                .map(mapper::toDomain);
    }
}