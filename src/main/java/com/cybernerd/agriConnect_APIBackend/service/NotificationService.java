package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.model.Notification;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    Notification notifier(Utilisateur destinataire, String message, String type);
    List<Notification> listerNotifications(Utilisateur destinataire);
    void marquerCommeLue(UUID notificationId);
    void marquerToutCommeLu(Utilisateur destinataire);
    Notification getNotificationById(UUID notificationId);
} 