package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormateurRequest {
    @NotBlank
    private String nomPrenom;

    @NotBlank
    private String specialite;

    @NotBlank
    private String direction;

    @NotBlank
    private String entreprise;
}
