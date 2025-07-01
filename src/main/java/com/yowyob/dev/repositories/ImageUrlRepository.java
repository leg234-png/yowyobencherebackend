package com.yowyob.dev.repositories;

import com.yowyob.dev.models.ImageUrl;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ImageUrlRepository extends R2dbcRepository<ImageUrl, UUID> {
    Flux<ImageUrl> findByAuctionId(UUID auctionId);
    Mono<Void> deleteByAuctionId(Long auctionId);
}