package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "participants")
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private String nomPrenom;

    @Column(nullable = false)
    private String cin;

    @Column(nullable = false)
    private String entreprise;

    private String telFix;

    private String fax;

    private String telPort;

    private String mail;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private Integer numSalle;

    @Column(nullable = false)
    private LocalDate dateDebut;
}
