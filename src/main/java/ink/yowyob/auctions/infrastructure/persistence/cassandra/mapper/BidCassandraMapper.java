//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/mapper/BidCassandraMapper.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.mapper;

import ink.yowyob.auctions.domain.model.Bid;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.BidEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BidCassandraMapper {

    @Mapping(target = "id", source = "key.bidId")
    @Mapping(target = "auctionId", source = "key.auctionId")
    @Mapping(target = "createdAt", source = "key.createdAt")
    Bid toDomain(BidEntity entity);

    @Mapping(target = "key.bidId", source = "id")
    @Mapping(target = "key.auctionId", source = "auctionId")
    @Mapping(target = "key.createdAt", source = "createdAt")
    BidEntity toEntity(Bid domain);
}
