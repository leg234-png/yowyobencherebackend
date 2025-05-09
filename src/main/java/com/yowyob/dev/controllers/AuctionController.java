package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.requestDTO.AuctionUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.services.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auction")
@AllArgsConstructor
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

    @Operation(summary = "have all of a user's auctions",
            description = "have all of a user's auctions with his username")
    @GetMapping("/seller/{username}")
    public ApiError getAuctionsBySellerUsername(@Parameter(
            description = "Username of user",
            required = true)@PathVariable String username) {
        return auctionService.getAuctionsBySellerUsername(username);
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



}
