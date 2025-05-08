package com.yowyob.dev.models;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Column(name = "type")
    private String type;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
