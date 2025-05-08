package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OffreRepository extends JpaRepository<Offre, UUID> {
    List<Offre> findByUtilisateurId(UUID utilisateurId);
    List<Offre> findByEnchereId(UUID enchereId);
}
