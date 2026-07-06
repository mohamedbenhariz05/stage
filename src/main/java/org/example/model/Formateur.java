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

@Data
@Entity
@Table(name = "formateurs")
@NoArgsConstructor
@AllArgsConstructor
public class Formateur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private String nomPrenom;

    @Column(nullable = false)
    private String specialite;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false)
    private String entreprise;
}
