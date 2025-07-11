//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/entity/CategoryEntity.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("categories")
public class CategoryEntity {
    @PrimaryKey
    private UUID id;
    private String name;
    private String description;
}
