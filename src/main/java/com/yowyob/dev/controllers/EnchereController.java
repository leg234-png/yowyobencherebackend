package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.requestDTO.EnchereDTO;
import com.yowyob.dev.dto.requestDTO.EnchereUpdateDTO;
import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.enumeration.EnchereStatus;
import com.yowyob.dev.services.EnchereService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/enchere")
@AllArgsConstructor
public class EnchereController {

    private final EnchereService enchereService;

    @PostMapping
    public ApiError create(@Valid @RequestBody EnchereDTO enchereDTO){
        return enchereService.create(enchereDTO);
    }

    @GetMapping("/{id}")
    public ApiError getEnchere(@PathVariable UUID id) {
        return enchereService.getEnchere(id);
    }

    @GetMapping("/seller/{id}")
    public ApiError getEncheresBySellerId(@PathVariable UUID id) {
        return enchereService.getEncheresBySellerId(id);
    }

    @PatchMapping("/{id}")
    public ApiError update(@PathVariable UUID id, @RequestBody EnchereUpdateDTO dto) {
        return enchereService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ApiError delete(@PathVariable UUID id) {
        return enchereService.delete(id);
    }

    @GetMapping("/category/{id}")
    public ApiError getEncheresByCategoryId(@PathVariable UUID id) {
        return enchereService.getEncheresByCategoryId(id);
    }

    @GetMapping("/user/{id}")
    public ApiError getEncheresByParticipantId(@PathVariable UUID id) {
        return enchereService.getEncheresByParticipants(id);
    }

    @GetMapping("/statut")
    public ApiError getEncheresByStatut(@RequestParam EnchereStatus status) {
        return enchereService.getEncheresByStatut(status);
    }
}
