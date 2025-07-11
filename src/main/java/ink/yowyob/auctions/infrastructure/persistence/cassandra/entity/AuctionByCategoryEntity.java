//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/entity/AuctionByCategoryEntity.java
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
@Table("auctions_by_category")
public class AuctionByCategoryEntity {

    @PrimaryKeyClass
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Key {
        @PrimaryKeyColumn(name = "category_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID categoryId;

        @PrimaryKeyColumn(name = "end_date", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
        private LocalDateTime endDate;

        @PrimaryKeyColumn(name = "auction_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private UUID auctionId;
    }

    @PrimaryKey
    private Key key;

    private String title;
    private BigDecimal currentPrice;
    private String status;
    private String firstImageUrl;
}
