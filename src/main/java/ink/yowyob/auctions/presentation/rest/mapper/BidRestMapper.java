//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/mapper/BidRestMapper.java
package ink.yowyob.auctions.presentation.rest.mapper;

import ink.yowyob.auctions.application.port.in.PlaceBidUseCase.PlaceBidCommand;
import ink.yowyob.auctions.domain.model.Bid;
import ink.yowyob.auctions.presentation.rest.dto.BidRequest;
import ink.yowyob.auctions.presentation.rest.dto.BidResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BidRestMapper {

    @Mapping(target = "auctionId", source = "auctionId")
    @Mapping(target = "bidderUsername", source = "username")
    PlaceBidCommand toCommand(BidRequest request, UUID auctionId, String username);

    BidResponse toResponse(Bid bid);
}