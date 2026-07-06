package org.example.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.CycleRequest;
import org.example.dto.CycleResponse;
import org.example.service.CycleService;
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
@RequestMapping("/cycles")
@RequiredArgsConstructor
public class CycleController {
    private final CycleService cycleService;

    @PostMapping
    public ResponseEntity<CycleResponse> create(@Valid @RequestBody CycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CycleResponse>> findAll() {
        return ResponseEntity.ok(cycleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CycleResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cycleService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CycleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CycleRequest request
    ) {
        return ResponseEntity.ok(cycleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        cycleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
