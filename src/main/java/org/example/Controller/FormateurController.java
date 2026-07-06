package org.example.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.FormateurRequest;
import org.example.dto.FormateurResponse;
import org.example.service.FormateurService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/formateurs")
@RequiredArgsConstructor
public class FormateurController {
    private final FormateurService formateurService;

    @PostMapping
    public ResponseEntity<FormateurResponse> create(@Valid @RequestBody FormateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formateurService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FormateurResponse>> findAll() {
        return ResponseEntity.ok(formateurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormateurResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(formateurService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormateurResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody FormateurRequest request
    ) {
        return ResponseEntity.ok(formateurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        formateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
