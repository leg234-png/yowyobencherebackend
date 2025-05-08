package com.yowyob.dev.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "agences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @ManyToMany
    @JoinTable(
        name = "agence_membres",
        joinColumns = @JoinColumn(name = "agence_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> membres = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
