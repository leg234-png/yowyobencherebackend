package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.requestDTO.AuctionUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.models.Category;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.repositories.AuctionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    public AuctionService(AuctionRepository auctionRepository, CategoryRepository categoryRepository, RestTemplate restTemplate) {
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.restTemplate = restTemplate;

    }


    public ApiError create(AuctionDTO auctionDTO) {
        ApiError apiError = new ApiError();

        Auction auction = new Auction();
        auction.setCreatedAt(LocalDateTime.now());
        auction.setItemCondition(auctionDTO.getItemCondition());

        Optional<Category> optionalCategory = categoryRepository.findById(auctionDTO.getCategoryId());
        if (optionalCategory.isEmpty()) {
            apiError.setMessage("Category not found");
            apiError.setCode("404");
            return apiError;
        }
        auction.setCategory(optionalCategory.get());
        auction.setEndDate(auctionDTO.getEndDate());
        auction.setStartDate(auctionDTO.getStartDate());
        auction.setImageUrl(auctionDTO.getImageUrl());
        auction.setStartingPrice(auctionDTO.getStartingPrice());
        auction.setDescription(auctionDTO.getDescription());
        auction.setTitle(auctionDTO.getTitle());


        if (verifierUtilisateurExiste(auctionDTO.getSellerUsername())) {
            apiError.setMessage("seller not found");
            apiError.setCode("404");
            return apiError;
        }
        auction.setSellerUsername(auctionDTO.getSellerUsername());
        auction.setStatus(AuctionStatus.OPEN);

        apiError.setMessage("L'enchere a été crée avec succès!");
        apiError.setCode("200");
        apiError.setData(auctionRepository.save(auction));
        return apiError;
    }

    public ApiError getAuction(UUID id) {
        ApiError apiError = new ApiError();

        Optional<Auction> optionalAuction = auctionRepository.findById(id);
        if(optionalAuction.isEmpty()) {
            apiError.setMessage("Auction not found");
            apiError.setCode("404");
            return apiError;
        }
        apiError.setMessage("Auction get successfully");
        apiError.setCode("200");
        apiError.setData(optionalAuction.get());
        return apiError;
    }

    public ApiError getAuctionsBySellerUsername(String username) {
        ApiError apiError = new ApiError();


        if (verifierUtilisateurExiste(username)) {
            apiError.setMessage("seller not found");
            apiError.setCode("404");
            return apiError;
        }
        List<Auction> auctions = auctionRepository.findBySellerUsername(username);

        apiError.setMessage("success");
        apiError.setCode("200");
        apiError.setData(auctions);
        return apiError;
    }

    public ApiError update(UUID id, AuctionUpdateDTO dto) {
        ApiError apiError = new ApiError();

        Optional<Auction> optional = auctionRepository.findById(id);
        if (optional.isEmpty()) {
            apiError.setMessage("Auction not found");
            apiError.setCode("404");
            return apiError;
        }

        Auction auction = optional.get();

        if (dto.getTitle() != null) {
            auction.setTitle(dto.getTitle());
        }

        if(dto.getStatus() != null) {
            auction.setStatus(dto.getStatus());
        }

        if (dto.getDescription() != null) {
            auction.setDescription(dto.getDescription());
        }

        if (dto.getStartingPrice() != null) {
            auction.setStartingPrice(dto.getStartingPrice());
        }

        if (dto.getStartDate() != null) {
            auction.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            auction.setEndDate(dto.getEndDate());
        }

        if (dto.getImageUrl() != null) {
            auction.setImageUrl(dto.getImageUrl());
        }

        if (dto.getItemCondition() != null) {
            auction.setItemCondition(dto.getItemCondition());
        }

        if (dto.getCategory_id() != null){
            Optional<Category> optionalCategory = categoryRepository.findById(dto.getCategory_id());
            if (optionalCategory.isEmpty()) {
                apiError.setMessage("Category not found");
                apiError.setCode("404");
                return apiError;
            }
            auction.setCategory(optionalCategory.get());
        }

        auctionRepository.save(auction);

        apiError.setMessage("Auction updated successfully");
        apiError.setCode("200");
        return apiError;
    }

    public ApiError delete(UUID id) {
        ApiError apiError = new ApiError();

        Optional<Auction> optional = auctionRepository.findById(id);
        if (optional.isEmpty()) {
            apiError.setMessage("Auction not found");
            apiError.setCode("404");
            return apiError;
        }

        auctionRepository.delete(optional.get());
        apiError.setMessage("auction deleted successfully");
        apiError.setCode("204");
        return apiError;

    }

    public ApiError getAuctionsByCategoryId(UUID id) {

        ApiError apiError = new ApiError();

        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            apiError.setMessage("Category not found");
            apiError.setCode("404");
            return apiError;
        }

        List<Auction> auctions = auctionRepository.findByCategory_Id(id);
        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(auctions);

        return apiError;

    }

    public ApiError getAuctionsByParticipants(String username) {
        ApiError apiError = new ApiError();


        if(verifierUtilisateurExiste(username)) {
            apiError.setMessage("Participant not found");
            apiError.setCode("404");
            return apiError;
        }

        List<Auction> auctions = auctionRepository.findByParticipant(username);
        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(auctions);

        return apiError;

    }

    public ApiError getAuctionsByStatut(AuctionStatus status) {
        ApiError apiError = new ApiError();

        List<Auction> auctions = auctionRepository.findByStatus(status);

        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(auctions);
        return apiError;
    }


    public boolean verifierUtilisateurExiste(String username) {
        String url = "https://gateway.yowyob.com/auth-service/auth/user/username/" + username;

        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);

            // Si on arrive ici et que le status est 200 OK, l'utilisateur existe
            return response.getStatusCode() != HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Autre problème de connexion ou erreur serveur
            throw new RuntimeException("Impossible de contacter le service utilisateur", e);
        }
    }


    public ApiError getCategories() {
        ApiError apiError = new ApiError();

        apiError.setData(categoryRepository.findAll());
        apiError.setMessage("list of categories");
        apiError.setCode("200");

        return apiError;
    }
}
