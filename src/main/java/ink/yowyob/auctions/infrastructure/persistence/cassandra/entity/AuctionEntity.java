//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/entity/AuctionEntity.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("auctions")
public class AuctionEntity {
    @PrimaryKey
    private UUID id;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // Enum est stocké en String
    private UUID agencyId;
    private UUID categoryId;
    private String itemCondition;
    private Set<String> imageUrls;
    private Set<String> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
