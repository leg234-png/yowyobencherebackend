package ink.yowyob.auctions.domain.model;


import lombok.Data;

import java.util.UUID;

@Data
public class ImageUrl {
    private UUID id;

    private UUID auctionId;

    private String url;
}
