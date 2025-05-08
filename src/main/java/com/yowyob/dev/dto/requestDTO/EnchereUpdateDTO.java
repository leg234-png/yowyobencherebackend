package com.yowyob.dev.dto.requestDTO;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EnchereUpdateDTO {

    private String titre;

    private String description;

    private Double prixInitial;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    private String imageUrl;

    private String condition;

    private UUID categorie_id;
}
