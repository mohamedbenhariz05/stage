package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CycleResponse {
    private Integer id;
    private String numAct;
    private String theme;
    private LocalDate dateDeb;
    private LocalDate dateFin;
    private String form1;
    private String form2;
    private String form3;
    private Integer numSalle;
}
