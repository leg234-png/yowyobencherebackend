package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.AgenceDTO;
import com.yowyob.dev.dto.requestDTO.AgenceUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgenceService {
    public ApiError getAgencyById(UUID id) {
        return null;
    }

    public ApiError create(@Valid AgenceDTO dto) {
        return null;
    }

    public ApiError update(UUID id, AgenceUpdateDTO dto) {
        return null;
    }

    public ApiError deleteAgency(UUID id) {
        return null;
    }

    public ApiError getAllAgencies() {
        return null;
    }
}
