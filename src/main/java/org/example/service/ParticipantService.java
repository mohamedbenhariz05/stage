package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ParticipantRequest;
import org.example.dto.ParticipantResponse;
import org.example.model.Participant;
import org.example.repo.ParticipantRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {
    private final ParticipantRepo participantRepository;

    public ParticipantResponse create(ParticipantRequest request) {
        Participant participant = new Participant();
        applyRequest(participant, request);
        return toResponse(participantRepository.save(participant));
    }

    public List<ParticipantResponse> findAll(String theme, Integer numSalle, LocalDate dateDebut) {
        List<Participant> participants;

        if (theme != null && !theme.isBlank()) {
            participants = participantRepository.findByTheme(theme);
        } else if (numSalle != null) {
            participants = participantRepository.findByNumSalle(numSalle);
        } else if (dateDebut != null) {
            participants = participantRepository.findByDateDebut(dateDebut);
        } else {
            participants = participantRepository.findAll();
        }

        return participants.stream()
                .map(this::toResponse)
                .toList();
    }

    public ParticipantResponse findById(Integer id) {
        return toResponse(findEntity(id));
    }

    public ParticipantResponse update(Integer id, ParticipantRequest request) {
        Participant participant = findEntity(id);
        applyRequest(participant, request);
        return toResponse(participantRepository.save(participant));
    }

    public void delete(Integer id) {
        Participant participant = findEntity(id);
        participantRepository.delete(participant);
    }

    private Participant findEntity(Integer id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
    }

    private void applyRequest(Participant participant, ParticipantRequest request) {
        participant.setNomPrenom(request.getNomPrenom());
        participant.setCin(request.getCin());
        participant.setEntreprise(request.getEntreprise());
        participant.setTelFix(request.getTelFix());
        participant.setFax(request.getFax());
        participant.setTelPort(request.getTelPort());
        participant.setMail(request.getMail());
        participant.setTheme(request.getTheme());
        participant.setNumSalle(request.getNumSalle());
        participant.setDateDebut(request.getDateDebut());
    }

    private ParticipantResponse toResponse(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getNomPrenom(),
                participant.getCin(),
                participant.getEntreprise(),
                participant.getTelFix(),
                participant.getFax(),
                participant.getTelPort(),
                participant.getMail(),
                participant.getTheme(),
                participant.getNumSalle(),
                participant.getDateDebut()
        );
    }
}
