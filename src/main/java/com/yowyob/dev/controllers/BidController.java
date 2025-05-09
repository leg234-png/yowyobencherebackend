package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.BidUpdateDTO;
import com.yowyob.dev.dto.requestDTO.BidDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.services.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/bid")
@AllArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ApiError createBid(@Valid @RequestBody BidDTO dto) {
        return bidService.createBid(dto);

    }

    @GetMapping("/{id}")
    public ApiError getBid(@PathVariable UUID id) {
       return bidService.getBid(id);

    }

    @GetMapping
    public ApiError getAllBids() {
        return bidService.getAllBids();

    }

    @PatchMapping("/{id}")
    public ApiError updateBid(@PathVariable UUID id, @RequestBody BidUpdateDTO dto) {
        return bidService.updateBid(id, dto);

    }

    @DeleteMapping("/{id}")
    public ApiError deleteBid(@PathVariable UUID id) {
        return bidService.deleteBid(id);
    }

    @Operation(summary = "get the participants",
            description = "This route allows you to have the 5 participants in descending order")
    @GetMapping("/participants/{id}")
    public ApiError getParticipants( @Parameter(
            description = "ID of the auction whose participants we want to have",
            required = true) @PathVariable UUID id){
        return bidService.getParticipants(id);
    }

    @Operation(summary = "get all bids of auction",
            description = "This route allows you to have all bids of an auction")
    @GetMapping("/auction/{auctionId}")
    public ApiError getAllBids(@Parameter(
            description = "Id of Auction",
            required = true)
        @PathVariable UUID auctionId
    ){
        return bidService.getAllBidsOfAuction(auctionId);
    }

    //TODO ajouter les annotations pour le swagger
}
