package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.OrganisationDTO;
import com.yowyob.dev.dto.requestDTO.OrganisationUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganisationService {

    public ApiError create(@Valid OrganisationDTO dto) {
        return null;
    }

    public ApiError getAllOrganisations() {
        return null;
    }

    public ApiError getOrganisation(UUID id) {
        return null;
    }

    public ApiError update(UUID id, OrganisationUpdateDTO dto) {
        return null;
    }
}
