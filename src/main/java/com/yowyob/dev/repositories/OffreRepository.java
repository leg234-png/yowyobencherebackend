package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OffreRepository extends JpaRepository<Offre, UUID> {
    List<Offre> findByUtilisateurId(UUID utilisateurId);
    List<Offre> findByEnchereId(UUID enchereId);
}
