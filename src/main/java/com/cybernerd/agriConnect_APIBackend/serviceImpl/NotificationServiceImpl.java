package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.model.Notification;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.repository.NotificationRepository;
import com.cybernerd.agriConnect_APIBackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public Notification notifier(Utilisateur destinataire, String message, String type) {
        Notification notif = Notification.builder()
                .destinataire(destinataire)
                .message(message)
                .type(type)
                .build();
        return notificationRepository.save(notif);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listerNotifications(Utilisateur destinataire) {
        return notificationRepository.findByDestinataireOrderByDateCreationDesc(destinataire);
    }

    @Override
    public void marquerCommeLue(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notif -> {
            notif.setLu(true);
            notificationRepository.save(notif);
        });
    }

    @Override
    public void marquerToutCommeLu(Utilisateur destinataire) {
        List<Notification> notifs = notificationRepository.findByDestinataireOrderByDateCreationDesc(destinataire);
        for (Notification notif : notifs) {
            if (!notif.isLu()) {
                notif.setLu(true);
                notificationRepository.save(notif);
            }
        }
    }

    @Override
    public Notification getNotificationById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
    }
} 