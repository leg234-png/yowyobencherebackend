package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Enchere;
import com.yowyob.dev.models.EnchereStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public interface EnchereRepository extends JpaRepository<Enchere, UUID> {
    List<Enchere> findByVendeurId(UUID vendeurId);
    List<Enchere> findByParticipants_Id(UUID userId);
    List<Enchere> findByStatut(EnchereStatus statut);
    List<Enchere> findByCategorie_Id(UUID categorieId);
    List<Enchere> findTop10ByDateFinAfterOrderByDateFinAsc(LocalDateTime now);
    List<Enchere> findTop10ByOrderByCreatedAtDesc();
    List<Enchere> findTop10ByOffresSizeGreaterThanOrderByOffresSizeDesc(int size);
}
