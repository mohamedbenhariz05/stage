package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FormateurResponse {
    private Integer id;
    private String nomPrenom;
    private String specialite;
    private String direction;
    private String entreprise;
}
