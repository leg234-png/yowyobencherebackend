//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/mapper/AuctionRestMapper.java
package ink.yowyob.auctions.presentation.rest.mapper;

import ink.yowyob.auctions.application.port.in.CreateAuctionUseCase.CreateAuctionCommand;
import ink.yowyob.auctions.domain.model.Auction;
import ink.yowyob.auctions.presentation.rest.dto.AuctionRequest;
import ink.yowyob.auctions.presentation.rest.dto.AuctionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = CategoryRestMapper.class)
public interface AuctionRestMapper {

    @Mapping(target = "agencyId", source = "agencyId")
    CreateAuctionCommand toCommand(AuctionRequest request, UUID agencyId);

    AuctionResponse toResponse(Auction auction);
}