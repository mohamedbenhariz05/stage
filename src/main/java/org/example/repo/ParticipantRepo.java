package org.example.repo;

import org.example.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ParticipantRepo extends JpaRepository<Participant, Integer> {
    List<Participant> findByTheme(String theme);

    List<Participant> findByDateDebut(LocalDate dateDebut);

    List<Participant> findByNumSalle(Integer numSalle);
}
