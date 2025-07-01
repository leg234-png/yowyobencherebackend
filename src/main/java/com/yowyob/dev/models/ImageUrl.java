package com.yowyob.dev.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("image_urls")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrl {
    @Id
    private UUID id;

    @Column("auction_id")
    private UUID auctionId;

    @Column("url")
    private String url;
}
