package org.example.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.ParticipantRequest;
import org.example.dto.ParticipantResponse;
import org.example.service.ParticipantService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
public class ParticipantController {
    private final ParticipantService participantService;

    @PostMapping
    public ResponseEntity<ParticipantResponse> create(@Valid @RequestBody ParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participantService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> findAll(
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Integer numSalle,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut
    ) {
        return ResponseEntity.ok(participantService.findAll(theme, numSalle, dateDebut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(participantService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipantResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ParticipantRequest request
    ) {
        return ResponseEntity.ok(participantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        participantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
