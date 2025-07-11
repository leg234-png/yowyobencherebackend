//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/mapper/CategoryRestMapper.java
package ink.yowyob.auctions.presentation.rest.mapper;

import ink.yowyob.auctions.domain.model.Category;
import ink.yowyob.auctions.presentation.rest.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryRestMapper {
    CategoryResponse toResponse(Category category);
}