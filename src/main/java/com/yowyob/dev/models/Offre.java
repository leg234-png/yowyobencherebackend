package com.yowyob.dev.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "montant", nullable = false)
    private Double montant;

    @Column(name = "date_offre", nullable = false)
    private LocalDateTime dateOffre;

    @ManyToOne
    @JoinColumn(name = "enchere_id", nullable = false)
    private Enchere enchere;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
