package com.yowyob.dev.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("bid")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    private UUID id;
    private Double price;
    @Column("auction_id")
    private UUID auctionId;
    private String username;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_at")
    private LocalDateTime updatedAt;
}