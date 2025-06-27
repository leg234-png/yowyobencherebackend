package com.yowyob.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.responseDTO.AuctionResponseDTO;
import com.yowyob.dev.mapper.AuctionMapper;
import com.yowyob.dev.mapper.CategoryMapper;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.services.AuctionService;
import com.yowyob.dev.utils.JwtUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/auctions")
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;
    private final AuctionMapper auctionMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    public AuctionController(AuctionService auctionService,
                             AuctionMapper auctionMapper,
                             CategoryRepository categoryRepository,
                             CategoryMapper categoryMapper,
                             ObjectMapper objectMapper) {
        this.auctionService = auctionService;
        this.auctionMapper = auctionMapper;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('AGENCY') or hasRole('ADMIN')")
    public Mono<AuctionResponseDTO> createAuction(
            @RequestPart("request") String request,
            @RequestPart("images") Flux<FilePart> imageFiles) {

        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} creating auction", userInfo.getUsername()))
                .then(Mono.fromCallable(() -> objectMapper.readValue(request, AuctionDTO.class)))
                .flatMap(auctionDTO -> auctionService.createAuction(auctionDTO, imageFiles))
                .flatMap(this::buildResponseDTO)
                .doOnSuccess(auction -> log.info("Auction created: {}", auction.getId()))
                .doOnError(error -> log.error("Error creating auction: {}", error.getMessage()));
    }

    // Endpoint public - pas d'authentification requise
    @GetMapping("/{id}")
    public Mono<AuctionResponseDTO> getAuction(@PathVariable UUID id) {
        return auctionService.getAuctionById(id)
                .flatMap(this::buildResponseDTO);
    }

    // Endpoint public - pas d'authentification requise
    @GetMapping("/categories")
    public Flux<com.yowyob.dev.dto.responseDTO.CategoryDTO> getCategories() {
        return categoryRepository.findAll()
                .map(categoryMapper::toDTO);
    }

    // Endpoint public - pas d'authentification requise
    @GetMapping("/recent")
    public Mono<Page<AuctionResponseDTO>> getRecentAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "7") int days) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return auctionService.findRecentAuctions(pageable, days)
                .flatMap(pageEntity -> {
                    Flux<AuctionResponseDTO> dtoFlux = Flux.fromIterable(pageEntity.getContent())
                            .flatMap(this::buildResponseDTO);

                    return dtoFlux.collectList()
                            .map(dtoList -> new PageImpl<>(dtoList, pageable, pageEntity.getTotalElements()));
                });
    }

    // Endpoint public - pas d'authentification requise
    @GetMapping("/ending-soon")
    public Mono<Page<AuctionResponseDTO>> getAuctionsEndingSoon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "24") int hours) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("endDate").ascending());

        return auctionService.findAuctionsEndingSoon(pageable, hours)
                .flatMap(pageEntity -> {
                    Flux<AuctionResponseDTO> dtoFlux = Flux.fromIterable(pageEntity.getContent())
                            .flatMap(this::buildResponseDTO);

                    return dtoFlux.collectList()
                            .map(dtoList -> new PageImpl<>(dtoList, pageable, pageEntity.getTotalElements()));
                });
    }

    // Endpoint protégé - authentification requise
    @GetMapping("/my-auctions")
    @PreAuthorize("hasRole('USER') or hasRole('AGENCY') or hasRole('ADMIN')")
    public Flux<AuctionResponseDTO> getMyAuctions() {
        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} retrieving their auctions", userInfo.getUsername()))
                .flatMapMany(userInfo -> {
                    // TODO: Adapter selon votre logique - ici on suppose que l'agencyId correspond à l'userId
                    UUID agencyId = UUID.fromString(userInfo.getUserId());
                    return auctionService.getAuctionsByAgencyId(agencyId);
                })
                .flatMap(this::buildResponseDTO);
    }

    // Endpoint protégé - seul le propriétaire ou un admin peut modifier
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('AGENCY') or hasRole('ADMIN')")
    public Mono<AuctionResponseDTO> updateAuction(
            @PathVariable UUID id,
            @RequestBody AuctionDTO updateDto) {

        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} updating auction {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> auctionService.updateAuction(id, updateDto))
                .flatMap(this::buildResponseDTO);
    }

    // Endpoint protégé - seul le propriétaire ou un admin peut supprimer
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER') or hasRole('AGENCY') or hasRole('ADMIN')")
    public Mono<Void> deleteAuction(@PathVariable UUID id) {
        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} deleting auction {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> auctionService.deleteAuction(id));
    }

    private Mono<AuctionResponseDTO> buildResponseDTO(Auction auction) {
        AuctionResponseDTO dto = auctionMapper.toResponseDTO(auction);
        return categoryRepository.findById(auction.getCategoryId())
                .map(categoryMapper::toDTO)
                .doOnNext(dto::setCategory)
                .thenReturn(dto);
    }

}