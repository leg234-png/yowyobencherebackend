//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/persistence/cassandra/mapper/CategoryCassandraMapper.java
package ink.yowyob.auctions.infrastructure.persistence.cassandra.mapper;

import ink.yowyob.auctions.domain.model.Category;
import ink.yowyob.auctions.infrastructure.persistence.cassandra.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryCassandraMapper {
    Category toDomain(CategoryEntity entity);
    CategoryEntity toEntity(Category domain);
}
