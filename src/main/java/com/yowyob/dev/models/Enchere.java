package com.yowyob.dev.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "encheres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enchere {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prix_initial", nullable = false)
    private Double prixInitial;

    @Column(name = "prix_courant", nullable = false)
    private Double prixCourant;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private EnchereStatus statut;

    @ManyToOne
    @JoinColumn(name = "vendeur_id", nullable = false)
    private User vendeur;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "condition")
    private String condition;

    @OneToMany(mappedBy = "enchere", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offre> offres = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "enchere_participants",
        joinColumns = @JoinColumn(name = "enchere_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
