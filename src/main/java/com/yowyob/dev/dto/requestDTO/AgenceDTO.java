package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class AgenceDTO {

    @NotNull(message = "the name of agence cannot be null")
    private String nom;
}
