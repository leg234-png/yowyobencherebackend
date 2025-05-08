package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.OrganisationDTO;
import com.yowyob.dev.dto.requestDTO.OrganisationUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.services.OrganisationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organisation")
@AllArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping
    public ApiError create (@Valid @RequestBody OrganisationDTO dto) {
        return organisationService.create(dto);
    }

    @GetMapping("/{id}")
    public ApiError getOrganisation(@PathVariable UUID id) {
        return organisationService.getOrganisation(id);
    }

    @GetMapping
    public ApiError getAllOrganisations() {
        return organisationService.getAllOrganisations();
    }

    @PatchMapping("/{id}")
    public ApiError updateOrganisation(@PathVariable UUID id, @RequestBody OrganisationUpdateDTO dto) {
        return organisationService.update(id, dto);
    }

}
