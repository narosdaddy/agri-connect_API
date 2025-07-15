package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
} 