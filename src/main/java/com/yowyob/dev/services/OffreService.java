package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.OffreDTO;
import com.yowyob.dev.dto.requestDTO.OffreUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.models.Enchere;
import com.yowyob.dev.models.Offre;
import com.yowyob.dev.models.User;
import com.yowyob.dev.repositories.EnchereRepository;
import com.yowyob.dev.repositories.OffreRepository;
import com.yowyob.dev.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;
    private final EnchereRepository enchereRepository;
    private final UserRepository userRepository;
    
    public ApiError createOffre(OffreDTO dto) {
        ApiError response = new ApiError();

        Optional<Enchere> enchereOpt = enchereRepository.findById(dto.getEnchereId());
        if (enchereOpt.isEmpty()) {
            return new ApiError("404", "Enchère non trouvée", null);
        }

        Optional<User> userOpt = userRepository.findById(dto.getUtilisateurId());
        if (userOpt.isEmpty()) {
            return new ApiError("404", "Utilisateur non trouvé", null);
        }

        Offre offre = new Offre();
        offre.setMontant(dto.getMontant());
        offre.setEnchere(enchereOpt.get());
        offre.setUtilisateur(userOpt.get());
        offre.setCreatedAt(LocalDateTime.now());

        Offre savedOffre = offreRepository.save(offre);

        response.setCode("200");
        response.setMessage("Offre créée avec succès.");
        response.setData(savedOffre);

        return response;
    }

    public ApiError getOffre(UUID id) {
        ApiError response = new ApiError();
        Optional<Offre> offreOpt = offreRepository.findById(id);

        if (offreOpt.isEmpty()) {
            return new ApiError("404", "Offre non trouvée", null);
        }

        response.setCode("200");
        response.setMessage("Offre récupérée avec succès.");
        response.setData(offreOpt.get());
        return response;
    }

    public ApiError getAllOffres() {
        ApiError response = new ApiError();
        List<Offre> offres = offreRepository.findAll();

        response.setCode("200");
        response.setMessage("Liste des offres.");
        response.setData(offres);
        return response;
    }

    public ApiError updateOffre(UUID id, OffreUpdateDTO dto) {
        ApiError response = new ApiError();
        Optional<Offre> offreOpt = offreRepository.findById(id);

        if (offreOpt.isEmpty()) {
            return new ApiError("404", "Offre non trouvée", null);
        }

        Offre offre = offreOpt.get();

        if (dto.getMontant() != null) {
            offre.setMontant(dto.getMontant());
        }

        offreRepository.save(offre);

        response.setCode("200");
        response.setMessage("Offre mise à jour avec succès.");
        response.setData(offre);
        return response;
    }

    public ApiError deleteOffre(UUID id) {
        ApiError response = new ApiError();
        Optional<Offre> offreOpt = offreRepository.findById(id);

        if (offreOpt.isEmpty()) {
            return new ApiError("404", "Offre non trouvée", null);
        }

        offreRepository.delete(offreOpt.get());

        response.setCode("204");
        response.setMessage("Offre supprimée avec succès.");
        response.setData(null);
        return response;
    }
}
