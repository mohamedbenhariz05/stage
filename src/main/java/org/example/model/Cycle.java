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
@Table(name = "cycles")
@NoArgsConstructor
@AllArgsConstructor
public class Cycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String numAct;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private LocalDate dateDeb;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private String form1;

    private String form2;

    private String form3;

    @Column(nullable = false)
    private Integer numSalle;
}
