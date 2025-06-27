package com.yowyob.dev.mapper;

import com.yowyob.dev.dto.requestDTO.BidDTO;
import com.yowyob.dev.models.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BidMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Bid toBid(BidDTO bidDTO);
}