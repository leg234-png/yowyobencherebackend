package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
}
