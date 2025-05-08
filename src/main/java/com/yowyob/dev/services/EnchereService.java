package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.EnchereDTO;
import com.yowyob.dev.dto.requestDTO.EnchereUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.enumeration.EnchereStatus;
import com.yowyob.dev.models.Categorie;
import com.yowyob.dev.models.Enchere;
import com.yowyob.dev.models.User;
import com.yowyob.dev.repositories.CategorieRepository;
import com.yowyob.dev.repositories.EnchereRepository;
import com.yowyob.dev.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnchereService {

    private final EnchereRepository enchereRepository;
    private final CategorieRepository categorieRepository;
    private final UserRepository userRepository;

    public EnchereService(EnchereRepository enchereRepository, CategorieRepository categorieRepository, UserRepository userRepository) {
        this.enchereRepository = enchereRepository;
        this.categorieRepository = categorieRepository;
        this.userRepository = userRepository;
    }


    public ApiError create(EnchereDTO enchereDTO) {
        ApiError apiError = new ApiError();

        Enchere enchere = new Enchere();
        enchere.setCreatedAt(LocalDateTime.now());
        enchere.setCondition(enchereDTO.getCondition());

        Optional<Categorie> optionalCategorie = categorieRepository.findById(enchereDTO.getCategorieId());
        if (optionalCategorie.isEmpty()) {
            apiError.setMessage("Category not found");
            apiError.setCode("404");
            return apiError;
        }
        enchere.setCategorie(optionalCategorie.get());
        enchere.setDateFin(enchereDTO.getDateFin());
        enchere.setDateDebut(enchereDTO.getDateDebut());
        enchere.setImageUrl(enchereDTO.getImageUrl());
        enchere.setPrixInitial(enchereDTO.getPrixInitial());
        enchere.setDescription(enchereDTO.getDescription());
        enchere.setTitre(enchereDTO.getTitre());

        Optional<User> optionalSeller = userRepository.findById(enchereDTO.getVendeurId());
        if (optionalSeller.isEmpty()) {
            apiError.setMessage("seller not found");
            apiError.setCode("404");
            return apiError;
        }
        enchere.setVendeur(optionalSeller.get());
        enchere.setStatut(EnchereStatus.OUVERTE);

        apiError.setMessage("L'enchere a été crée avec succès!");
        apiError.setCode("200");
        apiError.setData(enchereRepository.save(enchere));
        return apiError;
    }

    public ApiError getEnchere(UUID id) {
        ApiError apiError = new ApiError();

        Optional<Enchere> optionalEnchere = enchereRepository.findById(id);
        if(optionalEnchere.isEmpty()) {
            apiError.setMessage("Enchere not found");
            apiError.setCode("404");
            return apiError;
        }
        apiError.setMessage("Enchere get successfully");
        apiError.setCode("200");
        apiError.setData(optionalEnchere.get());
        return apiError;
    }

    public ApiError getEncheresBySellerId(UUID id) {
        ApiError apiError = new ApiError();

        Optional<User> optionalSeller = userRepository.findById(id);
        if (optionalSeller.isEmpty()) {
            apiError.setMessage("seller not found");
            apiError.setCode("404");
            return apiError;
        }
        List<Enchere> encheres = enchereRepository.findByVendeurId(id);

        apiError.setMessage("success");
        apiError.setCode("200");
        apiError.setData(encheres);
        return apiError;
    }

    public ApiError update(UUID id, EnchereUpdateDTO dto) {
        ApiError apiError = new ApiError();

        Optional<Enchere> optional = enchereRepository.findById(id);
        if (optional.isEmpty()) {
            apiError.setMessage("Enchere not found");
            apiError.setCode("404");
            return apiError;
        }

        Enchere enchere = optional.get();

        if (dto.getTitre() != null) {
            enchere.setTitre(dto.getTitre());
        }

        if (dto.getDescription() != null) {
            enchere.setDescription(dto.getDescription());
        }

        if (dto.getPrixInitial() != null) {
            enchere.setPrixInitial(dto.getPrixInitial());
        }

        if (dto.getDateDebut() != null) {
            enchere.setDateDebut(dto.getDateDebut());
        }

        if (dto.getDateFin() != null) {
            enchere.setDateFin(dto.getDateFin());
        }

        if (dto.getImageUrl() != null) {
            enchere.setImageUrl(dto.getImageUrl());
        }

        if (dto.getCondition() != null) {
            enchere.setCondition(dto.getCondition());
        }

        if (dto.getCategorie_id() != null){
            Optional<Categorie> optionalCategorie = categorieRepository.findById(dto.getCategorie_id());
            if (optionalCategorie.isEmpty()) {
                apiError.setMessage("Category not found");
                apiError.setCode("404");
                return apiError;
            }
            enchere.setCategorie(optionalCategorie.get());
        }

        enchereRepository.save(enchere);

        apiError.setMessage("Enchere updated successfully");
        apiError.setCode("200");
        return apiError;
    }

    public ApiError delete(UUID id) {
        ApiError apiError = new ApiError();

        Optional<Enchere> optional = enchereRepository.findById(id);
        if (optional.isEmpty()) {
            apiError.setMessage("Enchere not found");
            apiError.setCode("404");
            return apiError;
        }

        enchereRepository.delete(optional.get());
        apiError.setMessage("enchere deleted successfully");
        apiError.setCode("204");
        return apiError;

    }

    public ApiError getEncheresByCategoryId(UUID id) {

        ApiError apiError = new ApiError();

        Optional<Categorie> optionalCategorie = categorieRepository.findById(id);
        if (optionalCategorie.isEmpty()) {
            apiError.setMessage("Category not found");
            apiError.setCode("404");
            return apiError;
        }

        List<Enchere> encheres = enchereRepository.findByCategorie_Id(id);
        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(encheres);

        return apiError;

    }

    public ApiError getEncheresByParticipants(UUID id) {
        ApiError apiError = new ApiError();

        Optional<User> optionalParticipant = userRepository.findById(id);
        if(optionalParticipant.isEmpty()) {
            apiError.setMessage("Participant not found");
            apiError.setCode("404");
            return apiError;
        }

        List<Enchere> encheres = enchereRepository.findByParticipants_Id(id);
        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(encheres);

        return apiError;

    }

    public ApiError getEncheresByStatut(EnchereStatus status) {
        ApiError apiError = new ApiError();

        List<Enchere> encheres = enchereRepository.findByStatut(status);

        apiError.setMessage("Success");
        apiError.setCode("200");
        apiError.setData(encheres);
        return apiError;
    }

    //TODO logique de déroulement d'une enchère
}
