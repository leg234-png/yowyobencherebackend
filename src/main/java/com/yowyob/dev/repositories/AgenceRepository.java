package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AgenceRepository extends JpaRepository<Agence, UUID> {
}
