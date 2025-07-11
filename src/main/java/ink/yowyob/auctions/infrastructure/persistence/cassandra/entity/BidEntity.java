//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/entity/BidEntity.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("bids_by_auction")
public class BidEntity {

    @PrimaryKeyClass
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Key {
        @PrimaryKeyColumn(name = "auction_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID auctionId;

        @PrimaryKeyColumn(name = "created_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private LocalDateTime createdAt;

        @PrimaryKeyColumn(name = "bid_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private UUID bidId;
    }

    @PrimaryKey
    private Key key;

    private String username;
    private BigDecimal price;
}
