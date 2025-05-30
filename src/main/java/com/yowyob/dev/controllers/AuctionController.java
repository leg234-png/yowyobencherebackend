package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.requestDTO.AuctionUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.services.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;



import com.yowyob.dev.models.Auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@RestController
@RequestMapping("/auction")
@AllArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;

    @Operation(summary = "create an auction",
            description = "create auction with the necessary values")
    @PostMapping
    public ApiError create(@Parameter(
            description = "new values to create an auction",
            required = true)@Valid @RequestBody AuctionDTO auctionDTO){
        return auctionService.create(auctionDTO);
    }

    @Operation(summary = "get an auction",
            description = "get auction with the corresponding id")
    @GetMapping("/{id}")
    public ApiError getAuction(@Parameter(
            description = "ID of the auction to get",
            required = true)@PathVariable UUID id) {
        return auctionService.getAuction(id);
    }

    @Operation(summary = "have all of an agency's auctions",
            description = "have all of an agency's auctions with his id")
    @GetMapping("/agency/{id}")
    public ApiError getAuctionsByAgencyId(@Parameter(
            description = "id of agency",
            required = true)@PathVariable UUID id) {
        return auctionService.getAuctionsByAgenceId(id);
    }

    @Operation(summary = "update an auction",
            description = "update auction with the corresponding id")
    @PatchMapping("/{id}")
    public ApiError update(@Parameter(
            description = "ID of the auction to update and new values ",
            required = true)@PathVariable UUID id, @RequestBody AuctionUpdateDTO dto) {
        return auctionService.update(id, dto);
    }

    @Operation(summary = "delete an auction",
            description = "delete auction with the corresponding id")
    @DeleteMapping("/{id}")
    public ApiError delete(@Parameter(
            description = "ID of the auction to delete",
            required = true)@PathVariable UUID id) {
        return auctionService.delete(id);
    }

    @Operation(summary = "get the auctions by category",
            description = "have all auctions with the corresponding category")
    @GetMapping("/category/{id}")
    public ApiError getAuctionsByCategoryId(@Parameter(
            description = "ID of the category",
            required = true)@PathVariable UUID id) {
        return auctionService.getAuctionsByCategoryId(id);
    }

    @Operation(summary = "get the auctions by username",
    description = "have all auctions where the user corresponding to the username participates")
    @GetMapping("/user/{username}")
    public ApiError getAuctionsByParticipantId(@Parameter(
            description = "Username of the participant",
            required = true)@PathVariable String username) {
        return auctionService.getAuctionsByParticipants(username);
    }

    @Operation(summary = "get the auction by status",
            description = "This route returns all auctions with the status as a parameter")
    @GetMapping("/statut")
    public ApiError getAuctionsByStatut(@Parameter(
            description = "Status of the auctions e.g: OUVERTE, CLOSE ",
            required = true)@RequestParam AuctionStatus status) {
        return auctionService.getAuctionsByStatut(status);
    }

    @Operation(summary = "get all the categories",
            description = "This route returns all the categories)")
    @GetMapping("/categories")
    public ApiError getCategories() {
        return auctionService.getCategories();
    }


    @Operation(
            summary = "Récupérer les enchères récemment ajoutées",
            description = "Retourne la liste des enchères créées récemment, triées par date de création décroissante"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des enchères récentes récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/recent")
    public ResponseEntity<ApiError> getRecentAuctions(
            @Parameter(description = "Numéro de la page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Nombre d'éléments par page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Nombre de jours pour considérer une enchère comme récente", example = "7")
            @RequestParam(defaultValue = "7") int days) {

        try {
            log.info("Récupération des enchères récentes - page: {}, size: {}, days: {}", page, size, days);

            if (page < 0 || size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(
                        ApiError.builder()
                                .code("INVALID_PARAMETERS")
                                .message("Paramètres invalides: page >= 0, size entre 1 et 100")
                                .data(null)
                                .build()
                );
            }

            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest().body(
                        ApiError.builder()
                                .code("INVALID_DAYS_PARAMETER")
                                .message("Le nombre de jours doit être entre 1 et 365")
                                .data(null)
                                .build()
                );
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Auction> recentAuctions = auctionService.findRecentAuctions(cutoffDate, pageable);

            log.info("Trouvé {} enchères récentes sur {} total",
                    recentAuctions.getNumberOfElements(), recentAuctions.getTotalElements());

            return ResponseEntity.ok(
                    ApiError.builder()
                            .code("SUCCESS")
                            .message("Enchères récentes récupérées avec succès")
                            .data(recentAuctions)
                            .build()
            );

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enchères récentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiError.builder()
                            .code("INTERNAL_ERROR")
                            .message("Erreur interne lors de la récupération des enchères récentes")
                            .data(null)
                            .build()
            );
        }
    }

    @Operation(
            summary = "Récupérer les enchères qui se terminent bientôt",
            description = "Retourne la liste des enchères actives qui se terminent dans les prochaines heures/jours, triées par date de fin croissante"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des enchères se terminant bientôt récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/ending-soon")
    public ResponseEntity<ApiError> getAuctionsEndingSoon(
            @Parameter(description = "Numéro de la page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Nombre d'éléments par page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Nombre d'heures pour considérer qu'une enchère se termine bientôt", example = "24")
            @RequestParam(defaultValue = "24") int hours) {

        try {
            log.info("Récupération des enchères se terminant bientôt - page: {}, size: {}, hours: {}", page, size, hours);

            if (page < 0 || size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(
                        ApiError.builder()
                                .code("INVALID_PARAMETERS")
                                .message("Paramètres invalides: page >= 0, size entre 1 et 100")
                                .data(null)
                                .build()
                );
            }

            if (hours <= 0 || hours > 168) { // Maximum une semaine (168 heures)
                return ResponseEntity.badRequest().body(
                        ApiError.builder()
                                .code("INVALID_HOURS_PARAMETER")
                                .message("Le nombre d'heures doit être entre 1 et 168")
                                .data(null)
                                .build()
                );
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTimeLimit = now.plusHours(hours);
            Pageable pageable = PageRequest.of(page, size, Sort.by("endDate").ascending());

            Page<Auction> endingSoonAuctions = auctionService.findAuctionsEndingSoon(
                    now, endTimeLimit, AuctionStatus.OPEN, pageable
            );

            log.info("Trouvé {} enchères se terminant bientôt sur {} total",
                    endingSoonAuctions.getNumberOfElements(), endingSoonAuctions.getTotalElements());

            return ResponseEntity.ok(
                    ApiError.builder()
                            .code("SUCCESS")
                            .message("Enchères se terminant bientôt récupérées avec succès")
                            .data(endingSoonAuctions)
                            .build()
            );

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enchères se terminant bientôt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiError.builder()
                            .code("INTERNAL_ERROR")
                            .message("Erreur interne lors de la récupération des enchères se terminant bientôt")
                            .data(null)
                            .build()
            );
        }
    }


}
