package com.yowyob.dev.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yowyob.dev.enumeration.AuctionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private Double startingPrice;

    private Double currentPrice;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    private UUID agencyId;

    @ManyToOne
    private Category category;

    private String imageUrl;

    private String ItemCondition;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "bid_order")
    private List<Bid> bids = new ArrayList<>();

    @ElementCollection
    @OrderColumn(name = "participant_order")
    private List<String> participants = new ArrayList<>();



    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
