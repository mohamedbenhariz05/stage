package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.CycleRequest;
import org.example.dto.CycleResponse;
import org.example.model.Cycle;
import org.example.repo.CycleRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CycleService {
    private final CycleRepo cycleRepository;

    public CycleResponse create(CycleRequest request) {
        if (cycleRepository.existsByNumAct(request.getNumAct())) {
            throw new IllegalArgumentException("Cycle number already exists");
        }

        if (request.getDateFin().isBefore(request.getDateDeb())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Cycle cycle = new Cycle();
        cycle.setNumAct(request.getNumAct());
        cycle.setTheme(request.getTheme());
        cycle.setDateDeb(request.getDateDeb());
        cycle.setDateFin(request.getDateFin());
        cycle.setForm1(request.getForm1());
        cycle.setForm2(request.getForm2());
        cycle.setForm3(request.getForm3());
        cycle.setNumSalle(request.getNumSalle());

        return toResponse(cycleRepository.save(cycle));
    }

    public List<CycleResponse> findAll() {
        return cycleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CycleResponse findById(Integer id) {
        return toResponse(findEntity(id));
    }

    public CycleResponse update(Integer id, CycleRequest request) {
        Cycle cycle = findEntity(id);

        if (!cycle.getNumAct().equals(request.getNumAct()) && cycleRepository.existsByNumAct(request.getNumAct())) {
            throw new IllegalArgumentException("Cycle number already exists");
        }

        if (request.getDateFin().isBefore(request.getDateDeb())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        applyRequest(cycle, request);
        return toResponse(cycleRepository.save(cycle));
    }

    public void delete(Integer id) {
        Cycle cycle = findEntity(id);
        cycleRepository.delete(cycle);
    }

    private Cycle findEntity(Integer id) {
        return cycleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cycle not found"));
    }

    private void applyRequest(Cycle cycle, CycleRequest request) {
        cycle.setNumAct(request.getNumAct());
        cycle.setTheme(request.getTheme());
        cycle.setDateDeb(request.getDateDeb());
        cycle.setDateFin(request.getDateFin());
        cycle.setForm1(request.getForm1());
        cycle.setForm2(request.getForm2());
        cycle.setForm3(request.getForm3());
        cycle.setNumSalle(request.getNumSalle());
    }

    private CycleResponse toResponse(Cycle cycle) {
        return new CycleResponse(
                cycle.getId(),
                cycle.getNumAct(),
                cycle.getTheme(),
                cycle.getDateDeb(),
                cycle.getDateFin(),
                cycle.getForm1(),
                cycle.getForm2(),
                cycle.getForm3(),
                cycle.getNumSalle()
        );
    }
}
