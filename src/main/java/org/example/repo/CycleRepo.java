package org.example.repo;

import org.example.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleRepo extends JpaRepository<Cycle, Integer> {
    boolean existsByNumAct(String numAct);
}
