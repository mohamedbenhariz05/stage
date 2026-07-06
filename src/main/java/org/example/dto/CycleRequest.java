package org.example.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleRequest {
    @NotBlank
    private String numAct;

    @NotBlank
    private String theme;

    @NotNull
    @FutureOrPresent
    private LocalDate dateDeb;

    @NotNull
    @FutureOrPresent
    private LocalDate dateFin;

    @NotBlank
    private String form1;

    private String form2;

    private String form3;

    @NotNull
    @Min(1)
    @Max(7)
    private Integer numSalle;
}
