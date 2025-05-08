package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.OffreUpdateDTO;
import com.yowyob.dev.dto.requestDTO.OffreDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.services.OffreService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/offre")
@AllArgsConstructor
public class OffreController {

    private final OffreService offreService;

    @PostMapping
    public ApiError createOffre(@Valid @RequestBody OffreDTO dto) {
        return offreService.createOffre(dto);

    }

    @GetMapping("/{id}")
    public ApiError getOffre(@PathVariable UUID id) {
       return offreService.getOffre(id);

    }

    @GetMapping
    public ApiError getAllOffres() {
        return offreService.getAllOffres();

    }

    @PatchMapping("/{id}")
    public ApiError updateOffre(@PathVariable UUID id, @RequestBody OffreUpdateDTO dto) {
        return offreService.updateOffre(id, dto);

    }

    @DeleteMapping("/{id}")
    public ApiError deleteOffre(@PathVariable UUID id) {
        return offreService.deleteOffre(id);
    }
}
