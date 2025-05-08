package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Enchere;
import com.yowyob.dev.enumeration.EnchereStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface EnchereRepository extends JpaRepository<Enchere, UUID> {
    List<Enchere> findByVendeurId(UUID vendeurId);
    List<Enchere> findByParticipants_Id(UUID userId);
    List<Enchere> findByStatut(EnchereStatus statut);
    List<Enchere> findByCategorie_Id(UUID categorieId);

}
