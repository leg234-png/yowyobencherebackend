package com.yowyob.dev.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.responseDTO.PageResponse;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.exceptions.NotFoundException;
import com.yowyob.dev.mapper.AuctionMapper;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.models.ImageUrl;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.repositories.ImageUrlRepository;
import com.yowyob.dev.security.CustomJwtAuthenticationConverter;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final AuthService authService;
    private final AuctionMapper auctionMapper;
    private final String uploadPath;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    @Value("${app.user-service.base-url:http://157.90.26.3:8032/api}")
    private String userServiceBaseUrl;
    private final ImageUrlRepository imageUrlRepository;

    public AuctionService(AuctionRepository auctionRepository,
                          CategoryRepository categoryRepository,
                          BidRepository bidRepository,
                          AuthService authService,
                          AuctionMapper auctionMapper,
                          R2dbcEntityTemplate r2dbcEntityTemplate,
                          @Value("${app.upload.path:uploads/}") String uploadPath,
                          @Value("${app.upload.base-url:http://157.90.26.3:8031/api/uploads/}") String uploadBaseUrl, ImageUrlRepository imageUrlRepository) {
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.bidRepository = bidRepository;
        this.authService = authService;
        this.auctionMapper = auctionMapper;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.uploadPath = uploadPath != null ? uploadPath : "uploads/";
        this.imageUrlRepository = imageUrlRepository;

        // Créer le répertoire d'upload s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(this.uploadPath));
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", this.uploadPath, e);
        }
    }

    @Transactional
    public Mono<Auction> createAuction(AuctionDTO auctionDTO, Flux<FilePart> imageFiles) {

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    Auction auction = auctionMapper.toAuction(auctionDTO);
                    auction.setId(UUID.randomUUID());
                    auction.setStatus(AuctionStatus.OPEN);
                    auction.setCreatedAt(LocalDateTime.now());
                    auction.setUpdatedAt(LocalDateTime.now());

                    Mono<Boolean> agencyExistsMono = authService.agencyExists(auction.getAgencyId().toString());
                    Mono<Boolean> categoryExistsMono = auction.getCategoryId() != null
                            ? categoryRepository.existsById(auction.getCategoryId())
                            : Mono.just(true);

                    return Mono.zip(agencyExistsMono, categoryExistsMono)
                            .flatMap(tuple -> {
                                Boolean agencyExists = tuple.getT1();
                                Boolean categoryExists = tuple.getT2();

                                if (!agencyExists) {
                                    log.warn("Agency not found: {}", auction.getAgencyId());
                                    return Mono.error(new NotFoundException("Agency not found"));
                                }

                                if (!categoryExists) {
                                    log.warn("Category not found: {}", auction.getCategoryId());
                                    return Mono.error(new NotFoundException("Category not found"));
                                }

                                // 1. Insérer l'auction
                                return r2dbcEntityTemplate.insert(Auction.class)
                                        .using(auction)
                                        .doOnSuccess(a -> log.info("Auction inserted with id: {}", a.getId()))
                                        // 2. Puis gérer les images
                                        .flatMap(savedAuction -> savePhotos(savedAuction.getId(), imageFiles)
                                                .then(Mono.just(savedAuction)));
                            })
                            .doOnSubscribe(sub -> log.debug("Validation en cours pour l'enchère : {}", auction));

                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")))
                .doOnSuccess(auction -> log.info("Auction created successfully: {}", auction.getId()))
                .doOnError(error -> log.error("Error creating auction: {}", error.getMessage()));
    }

    private Mono<Void> savePhotos(UUID auctionId, Flux<FilePart> imageFiles) {
        return imageFiles.flatMap(filePart -> {
                    String filename = UUID.randomUUID() + "_" + filePart.filename();
                    Path path = Paths.get("/uploads/" + filename);

                    return filePart.transferTo(path)
                            .then(Mono.fromCallable(() -> {
                                ImageUrl imageUrl = new ImageUrl();
                                imageUrl.setId(UUID.randomUUID());
                                imageUrl.setAuctionId(auctionId);
                                imageUrl.setUrl("/uploads/" + filename);
                                return imageUrl;
                            }));
                })
                .flatMap(imageUrl -> r2dbcEntityTemplate.insert(ImageUrl.class)
                        .using(imageUrl))
                .then();
    }


    public Mono<Auction> getAuctionById(UUID id) {
        return auctionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                .flatMap(this::enrichAuctionWithDetails);
    }

    public Mono<Auction> enrichAuctionWithDetails(Auction auction) {
        Mono<List<String>> participantsMono = bidRepository
                .findParticipantsByAuctionOrderByPriceDesc(auction.getId())
                .collectList();

        Mono<Auction> auctionMono = Mono.just(auction);

        return Mono.zip(auctionMono, participantsMono, (auc, participants) -> {
            auc.setParticipants(participants);
            return auc;
        });
    }

    public Flux<Auction> getAuctionsByAgencyId(UUID agencyId) {
        return authService.agencyExists(agencyId.toString())
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new NotFoundException("Agency not found"));
                    }
                    return auctionRepository.findByAgencyId(agencyId);
                });
    }

    public Mono<Page<Auction>> findRecentAuctions(Pageable pageable, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        Flux<Auction> auctions = auctionRepository.findRecentAuctions(
                cutoffDate,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        Mono<Long> total = auctionRepository.countRecentAuctions(cutoffDate);

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    public Mono<Page<Auction>> findAuctionsEndingSoon(Pageable pageable, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);

        Flux<Auction> auctions = auctionRepository.findEndingSoonAuctions(
                AuctionStatus.OPEN,
                now,
                endTime,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        Mono<Long> total = auctionRepository.countEndingSoonAuctions(
                AuctionStatus.OPEN,
                now,
                endTime
        );

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }



    /**
     * Met à jour une enchère (seulement par le propriétaire)
     */
    public Mono<Auction> updateAuction(UUID auctionId, AuctionDTO updateDto) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());

                    return auctionRepository.findById(auctionId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                            .flatMap(existingAuction -> {
                                // Vérifier que l'utilisateur peut modifier cette enchère
                                // TODO: Implémenter la logique de vérification du propriétaire

                                // Mettre à jour les champs
                                if (updateDto.getTitle() != null) {
                                    existingAuction.setTitle(updateDto.getTitle());
                                }
                                if (updateDto.getDescription() != null) {
                                    existingAuction.setDescription(updateDto.getDescription());
                                }
                                if (updateDto.getEndDate() != null) {
                                    existingAuction.setEndDate(updateDto.getEndDate());
                                }
                                existingAuction.setUpdatedAt(LocalDateTime.now());

                                return auctionRepository.save(existingAuction);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")));
    }

    /**
     * Supprime une enchère (seulement par le propriétaire)
     */
    public Mono<Void> deleteAuction(UUID auctionId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());
                    String role = authToken.getToken().getClaimAsString("libelle");

                    return auctionRepository.findById(auctionId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                            .flatMap(auction -> {
                                // Vérifier que l'utilisateur peut supprimer cette enchère
                                // TODO: Implémenter la logique de vérification du propriétaire
                                // ou vérifier si c'est un admin
                                if (!"ADMIN".equalsIgnoreCase(role)) {
                                    // Logique additionnelle pour vérifier le propriétaire
                                }

                                return auctionRepository.delete(auction);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")));
    }


    public Mono<Boolean> testAgencyExistsDirect(String agencyId) {
        String url = userServiceBaseUrl + "/agencies/" + agencyId;

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                                .doOnConnected(conn -> {
                                    conn.addHandlerLast(new ReadTimeoutHandler(3, TimeUnit.SECONDS));
                                    conn.addHandlerLast(new WriteTimeoutHandler(3, TimeUnit.SECONDS));
                                })))
                .build();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("✅ Réponse reçue : " + response))
                .map(response -> {
                    try {
                        // Parse manuellement le champ "value"
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(response);
                        return "200".equals(jsonNode.get("value").asText());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur parsing JSON: " + e.getMessage());
                        return false;
                    }
                });
    }

    // Méthode pour récupérer les images d'une enchère
    public Mono<List<String>> getImagesByAuctionId(UUID auctionId) {
        return imageUrlRepository.findByAuctionId(auctionId)
                .map(ImageUrl::getUrl)
                .collectList();
    }

    public Mono<PageResponse<Auction>> getPagedAuctions(Pageable pageable) {
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

        Flux<Auction> auctions = auctionRepository.findAllAuctionsPaged(size, offset);
        Mono<Long> total = auctionRepository.countAllAuctions();

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageResponse<>(
                        tuple.getT1(),
                        tuple.getT2(),
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                ));
    }

    public Mono<PageResponse<Auction>> getAuctionsByStatus(AuctionStatus status, Pageable pageable) {
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

        Flux<Auction> auctions = auctionRepository.findByStatus(status)
                .skip(offset)
                .take(size);

        Mono<Long> total = auctionRepository.countByStatus(status);

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageResponse<>(
                        tuple.getT1(),
                        tuple.getT2(),
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                ));
    }

    public Mono<PageResponse<Auction>> getAuctionsByCategory(UUID categoryId, Pageable pageable) {
        long offset = pageable.getOffset();
        int size = pageable.getPageSize();

        Flux<Auction> auctions = auctionRepository.findByCategoryId(categoryId)
                .skip(offset)
                .take(size);

        Mono<Long> total = auctionRepository.countByCategoryId(categoryId);

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageResponse<>(
                        tuple.getT1(),
                        tuple.getT2(),
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                ));
    }


}