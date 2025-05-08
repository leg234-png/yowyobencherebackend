package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.AgenceDTO;
import com.yowyob.dev.dto.requestDTO.AgenceUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.services.AgenceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agence")
@AllArgsConstructor
public class AgenceController {

    private final AgenceService agenceService;

    @PostMapping
    public ApiError create (@Valid @RequestBody AgenceDTO dto) {
        return agenceService.create(dto);
    }

    @PatchMapping("/{id}")
    public ApiError update (@PathVariable UUID id, @RequestBody AgenceUpdateDTO dto) {
        return agenceService.update(id, dto);
    }

    @GetMapping("/{id}")
    public ApiError getAgencyById(@PathVariable UUID id) {
        return agenceService.getAgencyById(id);
    }

    @DeleteMapping("/{id}")
    public ApiError deleteAgency(@PathVariable UUID id){
        return agenceService.deleteAgency(id);
    }

    @GetMapping
    public ApiError getAllAgencies () {
        return agenceService.getAllAgencies();
    }


}
