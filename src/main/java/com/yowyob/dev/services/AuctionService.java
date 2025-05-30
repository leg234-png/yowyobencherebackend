package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.dto.requestDTO.AuctionUpdateDTO;
import com.yowyob.dev.dto.responseDTO.AgenceDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.dto.responseDTO.UserDTO;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.models.Category;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.repositories.AuctionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    public AuctionService(AuctionRepository auctionRepository, CategoryRepository categoryRepository, RestTemplate restTemplate) {
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.restTemplate = restTemplate;

    }


    public ApiError create(AuctionDTO auctionDTO) throws IOException {
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
        auction.setImageUrls(saveImages(auctionDTO.getImages()));
        auction.setStartingPrice(auctionDTO.getStartingPrice());
        auction.setDescription(auctionDTO.getDescription());
        auction.setTitle(auctionDTO.getTitle());


        AgenceDTO agenceDTO = verifierAgenceExiste(auctionDTO.getAgencyId());
        if (agenceDTO == null) {
            apiError.setMessage("agency not found");
            apiError.setCode("404");
            return apiError;
        }
        auction.setAgencyId(auctionDTO.getAgencyId());

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

    public ApiError getAuctionsByAgenceId(UUID id) {
        ApiError apiError = new ApiError();


        AgenceDTO agenceDTO = verifierAgenceExiste(id);
        if (agenceDTO == null) {
            apiError.setMessage("Agency not found");
            apiError.setCode("404");
            return apiError;
        }
        List<Auction> auctions = auctionRepository.findByAgencyId(id);

        apiError.setMessage("success");
        apiError.setCode("200");
        apiError.setData(auctions);
        return apiError;
    }

    public ApiError update(UUID id, AuctionUpdateDTO dto) throws IOException {
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

        if (dto.getImages() != null) {
            auction.setImageUrls(saveImages(dto.getImages()));
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

        UserDTO userDTO = verifierUtilisateurExiste(username);
        if(userDTO == null) {
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


    public UserDTO verifierUtilisateurExiste(String username ) {
        String url = "http://157.90.26.3:8032/api/user/" + username;

        try {
            ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);

            // Si on arrive ici et que le status est 200 OK, l'utilisateur existe
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            // Autre problème de connexion ou erreur serveur
            throw new RuntimeException("Impossible de contacter le service utilisateur", e);
        }
    }
    public AgenceDTO verifierAgenceExiste(UUID id) {
        String url = "http://157.90.26.3:8032/api/agencies/" + id;

        try {
            ResponseEntity<AgenceDTO> response = restTemplate.getForEntity(url, AgenceDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            // Autre problème de connexion ou erreur serveur
            throw new RuntimeException("Impossible de contacter le service ", e);
        }
    }


    public ApiError getCategories() {
        ApiError apiError = new ApiError();

        apiError.setData(categoryRepository.findAll());
        apiError.setMessage("list of categories");
        apiError.setCode("200");

        return apiError;
    }



    /**
     * Récupère les enchères créées récemment
     * @param cutoffDate Date limite pour considérer une enchère comme récente
     * @param pageable Paramètres de pagination
     * @return Page d'enchères récentes
     */
    public Page<Auction> findRecentAuctions(LocalDateTime cutoffDate, Pageable pageable) {
        log.debug("Recherche des enchères créées après: {}", cutoffDate);
        return auctionRepository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoffDate, pageable);
    }

    /**
     * Récupère les enchères actives qui se terminent bientôt
     * @param now Moment actuel
     * @param endTimeLimit Limite de temps pour considérer qu'une enchère se termine bientôt
     * @param status Statut des enchères à rechercher (généralement OPEN)
     * @param pageable Paramètres de pagination
     * @return Page d'enchères se terminant bientôt
     */
    public Page<Auction> findAuctionsEndingSoon(LocalDateTime now,
                                                LocalDateTime endTimeLimit,
                                                AuctionStatus status,
                                                Pageable pageable) {
        log.debug("Recherche des enchères {} se terminant entre {} et {}", status, now, endTimeLimit);
        return auctionRepository.findByStatusAndEndDateBetweenOrderByEndDateAsc(
                status, now, endTimeLimit, pageable
        );
    }


    public List<String> saveImages(List<MultipartFile> images) throws IOException {
        String uploadDir = "uploads/";
        List<String> imageUrls = new ArrayList<>();

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            imageUrls.add("http://157.90.26.3:8031/uploads/" + fileName);
        }

        return imageUrls;
    }

}

