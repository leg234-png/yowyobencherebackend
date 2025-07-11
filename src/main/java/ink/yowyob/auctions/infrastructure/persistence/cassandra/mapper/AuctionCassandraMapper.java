//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/mapper/AuctionCassandraMapper.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.mapper;

import ink.yowyob.auctions.domain.enumeration.AuctionStatus;
import ink.yowyob.auctions.domain.model.Auction;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionByAgencyEntity;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionByCategoryEntity;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.AuctionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuctionCassandraMapper {

    // Entité principale vers Domaine
    Auction toDomain(AuctionEntity entity);

    // Domaine vers Entité principale
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    AuctionEntity toEntity(Auction domain);

    // Domaine vers Entité de lookup (Agency)
    @Mapping(target = "key.agencyId", source = "agencyId")
    @Mapping(target = "key.endDate", source = "endDate")
    @Mapping(target = "key.auctionId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "firstImageUrl", source = "imageUrls", qualifiedByName = "getFirstImageUrl")
    AuctionByAgencyEntity toByAgencyEntity(Auction domain);

    // Domaine vers Entité de lookup (Category)
    @Mapping(target = "key.categoryId", source = "categoryId")
    @Mapping(target = "key.endDate", source = "endDate")
    @Mapping(target = "key.auctionId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "firstImageUrl", source = "imageUrls", qualifiedByName = "getFirstImageUrl")
    AuctionByCategoryEntity toByCategoryEntity(Auction domain);

    @Named("statusToString")
    default String statusToString(AuctionStatus status) {
        return status.name();
    }

    @Named("getFirstImageUrl")
    default String getFirstImageUrl(Set<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return imageUrls.iterator().next();
    }
}
