package org.example.repo;

import org.example.model.Formateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormateurRepo extends JpaRepository<Formateur, Integer> {
}
