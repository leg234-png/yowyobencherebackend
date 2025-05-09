package com.yowyob.dev.dto.requestDTO;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BidUpdateDTO {

    @Positive(message = "Le montant doit être supérieur à zéro.")
    private Double price;
}
