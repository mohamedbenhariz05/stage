package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.FormateurRequest;
import org.example.dto.FormateurResponse;
import org.example.model.Formateur;
import org.example.repo.FormateurRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormateurService {
    private final FormateurRepo formateurRepository;

    public FormateurResponse create(FormateurRequest request) {
        Formateur formateur = new Formateur();
        applyRequest(formateur, request);
        return toResponse(formateurRepository.save(formateur));
    }

    public List<FormateurResponse> findAll() {
        return formateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FormateurResponse findById(Integer id) {
        return toResponse(findEntity(id));
    }

    public FormateurResponse update(Integer id, FormateurRequest request) {
        Formateur formateur = findEntity(id);
        applyRequest(formateur, request);
        return toResponse(formateurRepository.save(formateur));
    }

    public void delete(Integer id) {
        Formateur formateur = findEntity(id);
        formateurRepository.delete(formateur);
    }

    private Formateur findEntity(Integer id) {
        return formateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
    }

    private void applyRequest(Formateur formateur, FormateurRequest request) {
        formateur.setNomPrenom(request.getNomPrenom());
        formateur.setSpecialite(request.getSpecialite());
        formateur.setDirection(request.getDirection());
        formateur.setEntreprise(request.getEntreprise());
    }

    private FormateurResponse toResponse(Formateur formateur) {
        return new FormateurResponse(
                formateur.getId(),
                formateur.getNomPrenom(),
                formateur.getSpecialite(),
                formateur.getDirection(),
                formateur.getEntreprise()
        );
    }
}
