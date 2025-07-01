//---> PATH: src/main/java/com/yowyob/dev/mapper/AuctionMapper.java (Nouveau)
package com.yowyob.dev.mapper;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.requestDTO.AuctionUpdateDTO;
import com.yowyob.dev.dto.responseDTO.AuctionResponseDTO;
import com.yowyob.dev.models.Auction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface AuctionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentPrice", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
   // @Mapping(target = "imageUrls", ignore = true)
    Auction toAuction(AuctionDTO auctionDTO);

    // Le champ "category" dans AuctionResponseDTO sera mappé en utilisant le CategoryMapper
    @Mapping(target = "imageUrls", ignore = true)
    AuctionResponseDTO toResponseDTO(Auction auction);

    void updateAuctionFromDto(AuctionUpdateDTO dto, @MappingTarget Auction auction);
}