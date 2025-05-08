package com.yowyob.dev.models;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Column(name = "type")
    private String type;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
