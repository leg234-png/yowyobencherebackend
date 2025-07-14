package com.yowyob.dev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.responseDTO.AuctionResponseDTO;
import com.yowyob.dev.dto.responseDTO.PageResponse;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.exceptions.InvalidRequestException;
import com.yowyob.dev.mapper.AuctionMapper;
import com.yowyob.dev.mapper.CategoryMapper;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.services.AuctionService;
import com.yowyob.dev.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @GetMapping("/test")
    public Mono<ResponseEntity<String>> check() {
        return auctionService.testAgencyExistsDirect("ae961770-2673-45f6-b3a1-744c2c5de6ed")
                .map(exists -> {
                    if (exists) return ResponseEntity.ok("✔️ Agence trouvée !");
                    else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Agence introuvable.");
                });
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuctionResponseDTO> createAuction(
            @RequestPart("auction") String auctionJSON,
            @RequestPart(value = "images", required = false) Flux<FilePart> imageFiles) {

        return parseAuctionRequest(auctionJSON) // ← Parsing manuel
                .flatMap(auctionDTO -> {
                    Flux<FilePart> safeImageFiles = imageFiles != null ? imageFiles : Flux.empty();
                    return auctionService.createAuction(auctionDTO, safeImageFiles);
                })
                .flatMap(this::buildResponseDTO)
                .onErrorMap(JsonProcessingException.class,
                        ex -> new InvalidRequestException("Invalid auction data format", ex));
    }

    private Mono<AuctionResponseDTO> buildResponseDTO(Auction auction) {
        Mono<List<String>> imagesMono = auctionService.getImagesByAuctionId(auction.getId());
        Mono<Auction> enrichedAuctionMono = auctionService.enrichAuctionWithDetails(auction);

        return Mono.zip(enrichedAuctionMono, imagesMono)
                .map(tuple -> {
                    Auction enrichedAuction = tuple.getT1();
                    List<String> imageUrls = tuple.getT2();

                    AuctionResponseDTO responseDTO = auctionMapper.toResponseDTO(enrichedAuction);
                    responseDTO.setImageUrls(imageUrls);
                    responseDTO.setParticipants(enrichedAuction.getParticipants());

                    return responseDTO;
                });
    }

    private Mono<AuctionDTO> parseAuctionRequest(String request) {
        return Mono.fromCallable(() -> {
            try {
                log.info("Parsing auction request: {}", request); // ← Debug log
                return objectMapper.readValue(request, AuctionDTO.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse auction request: {}", e.getMessage(), e); // ← Erreur détaillée
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic());
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
    public Mono<Void> deleteAuction(@PathVariable UUID id) {
        return JwtUtils.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("User {} deleting auction {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> auctionService.deleteAuction(id));
    }

//    private Mono<AuctionResponseDTO> buildResponseDTO(Auction auction) {
//        AuctionResponseDTO dto = auctionMapper.toResponseDTO(auction);
//        return categoryRepository.findById(auction.getCategoryId())
//                .map(categoryMapper::toDTO)
//                .doOnNext(dto::setCategory)
//                .thenReturn(dto);
//    }

    @GetMapping("/paged")
    @Operation(summary = "Lister les enchères paginées avec images")
    public Mono<ResponseEntity<PageResponse<AuctionResponseDTO>>> getPagedAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));

        return auctionService.getPagedAuctions(pageable)
                .flatMap(pageResult -> Flux.fromIterable(pageResult.getContent())
                        .flatMap(this::buildResponseDTO)
                        .collectList()
                        .map(dtoList -> {
                            PageResponse<AuctionResponseDTO> pageResponse = new PageResponse<>(
                                    dtoList,
                                    pageResult.getTotalElements(),
                                    pageResult.getPage(),
                                    pageResult.getSize()
                            );
                            return ResponseEntity.ok(pageResponse);
                        })
                );
    }



    @GetMapping("/by-status")
    @Operation(summary = "Lister les enchères par statut avec images")
    public Mono<ResponseEntity<PageResponse<AuctionResponseDTO>>> getAuctionsByStatus(
            @RequestParam AuctionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));

        return auctionService.getAuctionsByStatus(status, pageable)
                .flatMap(pageResult ->
                        Flux.fromIterable(pageResult.getContent())
                                .flatMap(this::buildResponseDTO)
                                .collectList()
                                .map(dtoList -> {
                                    PageResponse<AuctionResponseDTO> pageResponse = new PageResponse<>(
                                            dtoList,
                                            pageResult.getTotalElements(),
                                            pageResult.getPage(),
                                            pageResult.getSize()
                                    );
                                    return ResponseEntity.ok(pageResponse);
                                })
                );
    }



    @GetMapping("/by-category")
    @Operation(summary = "Lister les enchères par catégorie avec images")
    public Mono<ResponseEntity<PageResponse<AuctionResponseDTO>>> getAuctionsByCategory(
            @RequestParam UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));

        return auctionService.getAuctionsByCategory(categoryId, pageable)
                .flatMap(pageResult ->
                        Flux.fromIterable(pageResult.getContent())
                                .flatMap(this::buildResponseDTO)
                                .collectList()
                                .map(dtoList -> {
                                    PageResponse<AuctionResponseDTO> pageResponse = new PageResponse<>(
                                            dtoList,
                                            pageResult.getTotalElements(),
                                            pageResult.getPage(),
                                            pageResult.getSize()
                                    );
                                    return ResponseEntity.ok(pageResponse);
                                })
                );
    }

    @GetMapping("/participated")
    @Operation(summary = "Lister les enchères auxquelles je participe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des enchères participées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public Flux<AuctionResponseDTO> getMyParticipatedAuctions() {
        return JwtUtils.getCurrentUsername()
                .doOnNext(username -> log.info("User {} retrieving participated auctions", username))
                .flatMapMany(auctionService::getParticipatedAuctions)
                .flatMap(this::buildResponseDTO);
    }

    @GetMapping("/won")
    @Operation(summary = "Lister les enchères que j'ai gagnées")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des enchères gagnées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public Flux<AuctionResponseDTO> getMyWonAuctions() {
        return JwtUtils.getCurrentUsername()
                .doOnNext(username -> log.info("User {} retrieving won auctions", username))
                .flatMapMany(auctionService::getWonAuctions)
                .flatMap(this::buildResponseDTO);
    }

}