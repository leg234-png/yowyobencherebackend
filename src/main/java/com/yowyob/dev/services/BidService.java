package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.BidDTO;
import com.yowyob.dev.dto.requestDTO.BidUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.dto.responseDTO.UserDTO;
import com.yowyob.dev.models.Bid;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final RestTemplate restTemplate;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApiError createBid(BidDTO dto) {
        ApiError response = new ApiError();
        Optional<Auction> auctionOpt = auctionRepository.findById(dto.getAuctionId());
        if (auctionOpt.isEmpty()) {
            return new ApiError("404", "Auction not found", null);
        }

        if (!verifierUtilisateurExiste(dto.getUsername())) {
            return new ApiError("404", "user not found", null);
        }

        Auction auction = auctionOpt.get();

        if (dto.getPrice() < auction.getStartingPrice()) {
            response.setMessage("You cannot provide an amount less than the original amount of the item");
            response.setCode("400");
            return response;
        }

        Bid bid = new Bid();
        bid.setPrice(dto.getPrice());
        bid.setAuction(auction);
        bid.setUsername(dto.getUsername());
        bid.setCreatedAt(LocalDateTime.now());

        Bid savedBid = bidRepository.save(bid);

        int index = 0;

        LinkedList<Bid> bids = auction.getBids();
        LinkedList<String> participants = auction.getParticipants();

        // Parcourir les bids pour trouver la bonne position
        while (index < bids.size() &&
                bids.get(index).getPrice() > savedBid.getPrice()) {
            index++;
        }

        // Pour les égalités de montant
        while (index < bids.size() &&
                bids.get(index).getPrice().equals(savedBid.getPrice())) {
            index++;
        }

        // Insérer dans les deux listes à la même position
        bids.add(index, savedBid);
        participants.add(index, savedBid.getUsername());

        auction.setBids(bids);
        auction.setParticipants(participants);

        auctionRepository.save(auction);

        response.setCode("200");
        response.setMessage("Bid created");
        response.setData(savedBid);

        return response;
    }

    public ApiError getBid(UUID id) {
        ApiError response = new ApiError();
        Optional<Bid> bidOpt = bidRepository.findById(id);

        if (bidOpt.isEmpty()) {
            return new ApiError("404", "Bid not found", null);
        }

        response.setCode("200");
        response.setMessage("success");
        response.setData(bidOpt.get());
        return response;
    }

    public ApiError getAllBids() {
        ApiError response = new ApiError();
        List<Bid> bids = bidRepository.findAll();

        response.setCode("200");
        response.setMessage("bids list");
        response.setData(bids);
        return response;
    }

    public ApiError updateBid(UUID id, BidUpdateDTO dto) {
        ApiError response = new ApiError();
        Optional<Bid> bidOpt = bidRepository.findById(id);

        if (bidOpt.isEmpty()) {
            return new ApiError("404", "Bid not found", null);
        }

        Bid bid = bidOpt.get();

        if (dto.getPrice() != null) {
            bid.setPrice(dto.getPrice());
        }

        bidRepository.save(bid);

        response.setCode("200");
        response.setMessage("Bid updated successfully");
        response.setData(bid);
        return response;
    }

    public ApiError deleteBid(UUID id) {
        ApiError response = new ApiError();
        Optional<Bid> bidOpt = bidRepository.findById(id);

        if (bidOpt.isEmpty()) {
            return new ApiError("404", "Bid not found", null);
        }

        bidRepository.delete(bidOpt.get());

        response.setCode("204");
        response.setMessage("Bid deleted successfully.");
        response.setData(null);
        return response;
    }



    public boolean verifierUtilisateurExiste(String username) {
        String url = "https://gateway.yowyob.com/auth-service/auth/user/username/" + username;

        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);

            // Si on arrive ici et que le status est 200 OK, l'utilisateur existe
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Autre problème de connexion ou erreur serveur
            throw new RuntimeException("Impossible de contacter le service utilisateur", e);
        }
    }

    public ApiError getParticipants(UUID id) {
        ApiError response = new ApiError();

        List<String> usernames = bidRepository.findParticipantsByAuctionOrderByPriceAsc(id);

        if (usernames.isEmpty()) {
            response.setCode("404");
            response.setMessage("Aucun participant trouvé pour cette enchère.");
            response.setData(null);
            return response;
        }

        List<UserDTO> participants = new ArrayList<>();
        for (String username : usernames) {
            UserDTO userDTO = getUserByUsername(username);
            participants.add(userDTO);
        }


        response.setCode("200");
        response.setMessage("participants récupérés avec succès.");
        response.setData(participants);
        return response;
    }


    public UserDTO getUserByUsername(String username) {
        String url = "https://gateway.yowyob.com/auth-service/auth/user/username/" + username;

        try {
            ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);

            // Si on arrive ici et que le status est 200 OK, l'utilisateur existe
            if (response.getStatusCode() == HttpStatus.OK){
                return response.getBody();
            }
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            // Autre problème de connexion ou erreur serveur
            throw new RuntimeException("Impossible de contacter le service utilisateur", e);
        }
        return null;
    }


    public ApiError getAllBidsOfAuction(UUID auctionId) {
        ApiError apiError = new ApiError();

        List<Bid> bids = bidRepository.findByAuctionId(auctionId);

        if (bids.isEmpty()){
            apiError.setCode("204");
            apiError.setMessage("this auction does not have bids yet");
            return apiError;
        }

        apiError.setData(bids);
        apiError.setCode("200");
        apiError.setMessage("success");

        return apiError;
    }
}
