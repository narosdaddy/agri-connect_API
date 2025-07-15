package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Notification;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByDestinataireOrderByDateCreationDesc(Utilisateur destinataire);
    long countByDestinataireAndLuFalse(Utilisateur destinataire);
} 