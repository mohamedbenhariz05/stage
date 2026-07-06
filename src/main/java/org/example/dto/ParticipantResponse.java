package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ParticipantResponse {
    private Integer id;
    private String nomPrenom;
    private String cin;
    private String entreprise;
    private String telFix;
    private String fax;
    private String telPort;
    private String mail;
    private String theme;
    private Integer numSalle;
    private LocalDate dateDebut;
}
