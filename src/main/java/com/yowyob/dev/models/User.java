package com.yowyob.dev.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "rating")
    private Double rating = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    @ElementCollection
    @CollectionTable(name = "user_payment_methods", joinColumns = @JoinColumn(name = "user_id"))
    private List<PaymentMethod> methodePaiement = new ArrayList<>();
}
