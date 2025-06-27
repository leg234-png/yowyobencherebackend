package com.yowyob.dev.mapper;

import com.yowyob.dev.dto.responseDTO.CategoryDTO;
import com.yowyob.dev.models.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);
    Category toEntity(CategoryDTO dto);
}