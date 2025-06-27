package com.yowyob.dev.models;

import com.yowyob.dev.enumeration.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table("auction") // Annotation Spring Data R2DBC
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    @Id
    private UUID id;
    private String title;
    private String description;
    @Column("starting_price")
    private Double startingPrice;
    @Column("current_price")
    private Double currentPrice;
    @Column("start_date")
    private LocalDateTime startDate;
    @Column("end_date")
    private LocalDateTime endDate;
    private AuctionStatus status;
    @Column("agency_id")
    private UUID agencyId;
    @Column("category_id")
    private UUID categoryId;
    @Column("image_urls")
    private List<String> imageUrls = new ArrayList<>(); // R2DBC peut gérer les listes de types simples
    @Column("item_condition")
    private String itemCondition;

    @Transient // Indique que ce champ n'est pas mappé à une colonne de la table 'auction'
    private List<Bid> bids = new ArrayList<>();
    @Transient
    private List<String> participants = new ArrayList<>();

    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_at")
    private LocalDateTime updatedAt;
}