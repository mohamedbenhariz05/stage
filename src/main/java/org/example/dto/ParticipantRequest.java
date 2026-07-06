package org.example.dto;

import jakarta.validation.constraints.Email;
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
public class ParticipantRequest {
    @NotBlank
    private String nomPrenom;

    @NotBlank
    private String cin;

    @NotBlank
    private String entreprise;

    private String telFix;

    private String fax;

    private String telPort;

    @Email
    private String mail;

    @NotBlank
    private String theme;

    @NotNull
    @Min(1)
    @Max(7)
    private Integer numSalle;

    @NotNull
    @FutureOrPresent
    private LocalDate dateDebut;
}
