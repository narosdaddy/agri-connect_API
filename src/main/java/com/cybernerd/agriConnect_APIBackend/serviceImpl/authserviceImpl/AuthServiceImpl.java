package com.cybernerd.agriConnect_APIBackend.serviceImpl.authserviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.AuthResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.model.Acheteur;
import com.cybernerd.agriConnect_APIBackend.model.Producteur;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import com.cybernerd.agriConnect_APIBackend.service.authService.AuthService;
import com.cybernerd.agriConnect_APIBackend.security.CustomUserDetailsService;
import com.cybernerd.agriConnect_APIBackend.security.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JavaMailSender mailSender;
    private UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl( 
        JwtService jwtService, 
        CustomUserDetailsService userDetailsService,
        JavaMailSender mailSender,
        UtilisateurRepository utilisateurRepository) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mailSender = mailSender;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Encoder le mot de passe
        String motDePasseEncode = passwordEncoder.encode(request.getMotDePasse());

        // Créer l'utilisateur selon le rôle
        Utilisateur utilisateur;
        if (request.getRole().name().equals("PRODUCTEUR")) {
            Producteur producteur = new Producteur();
            producteur.setNom(request.getNom());
            producteur.setEmail(request.getEmail());
            producteur.setMotDePasse(motDePasseEncode);
            producteur.setRole(request.getRole());
            producteur.setTelephone(request.getTelephone());
            producteur.setAdresse(request.getAdresse());
            producteur.setVille(request.getVille());
            producteur.setCodePostal(request.getCodePostal());
            producteur.setPays(request.getPays());
            
            // Champs spécifiques au producteur
            producteur.setNomExploitation(request.getNomExploitation());
            producteur.setDescriptionExploitation(request.getDescriptionExploitation());
            producteur.setCertifieBio(request.isCertifieBio());
            producteur.setAdresseExploitation(request.getAdresseExploitation());
            producteur.setVilleExploitation(request.getVilleExploitation());
            producteur.setCodePostalExploitation(request.getCodePostalExploitation());
            producteur.setPaysExploitation(request.getPaysExploitation());
            producteur.setTelephoneExploitation(request.getTelephoneExploitation());
            
            utilisateur = producteur;
        } else {
            Acheteur acheteur = new Acheteur();
            acheteur.setNom(request.getNom());
            acheteur.setEmail(request.getEmail());
            acheteur.setMotDePasse(motDePasseEncode);
            acheteur.setRole(request.getRole());
            acheteur.setTelephone(request.getTelephone());
            acheteur.setAdresse(request.getAdresse());
            acheteur.setVille(request.getVille());
            acheteur.setCodePostal(request.getCodePostal());
            acheteur.setPays(request.getPays());
            
            utilisateur = acheteur;
        }

        // Générer un token de vérification d'email
        String emailVerificationToken = jwtService.generateEmailVerificationToken(utilisateur.getEmail());
        utilisateur.setEmailVerificationToken(emailVerificationToken);

        // Par défaut, l'email n'est pas vérifié
        utilisateur.setEmailVerifie(false);

        // Enregistrer l'utilisateur dans la base de données
        utilisateurRepository.save(utilisateur);
        
        // Envoyer l'email de vérification
        sendVerificationEmail(utilisateur.getEmail());
        
        // Retourner la réponse d'authentification
        return AuthResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .emailVerifie(utilisateur.isEmailVerifie())
                .adresse(utilisateur.getAdresse())
                .ville(utilisateur.getVille())
                .codePostal(utilisateur.getCodePostal())
                .pays(utilisateur.getPays())
                .build();
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        // Vérifier si l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si l'email est vérifié
        if (!utilisateur.isEmailVerifie()) {
            throw new RuntimeException("Veuillez vérifier votre adresse email avant de vous connecter.");
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(loginRequest.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Charger les UserDetails pour la génération du token
        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());

        // Générer le token JWT
        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Construire la réponse
        AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .token(jwt)
                .refreshToken(refreshToken)
                .emailVerifie(utilisateur.isEmailVerifie())
                .adresse(utilisateur.getAdresse())
                .ville(utilisateur.getVille())
                .codePostal(utilisateur.getCodePostal())
                .pays(utilisateur.getPays());

        // Ajouter les champs spécifiques aux producteurs
        if (utilisateur instanceof Producteur) {
            Producteur producteur = (Producteur) utilisateur;
            responseBuilder.nomExploitation(producteur.getNomExploitation())
                    .descriptionExploitation(producteur.getDescriptionExploitation())
                    .certifieBio(producteur.isCertifieBio())
                    .verifie(producteur.isVerifie());
        }

        return responseBuilder.build();
    }

    @Override
    public AuthResponse refreshToken(String token) {
        try {
            // Extraire l'email à partir du token existant
            String email = jwtService.extractUsername(token);
            if (email == null) {
                throw new RuntimeException("Token invalide");
            }

            // Charger les UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Vérifier la validité du token
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new RuntimeException("Token expiré ou invalide");
            }

            // Générer un nouveau token JWT
            String newToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            // Charger l'utilisateur pour les informations supplémentaires
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Construire la réponse
            AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                    .id(utilisateur.getId())
                    .nom(utilisateur.getNom())
                    .email(utilisateur.getEmail())
                    .telephone(utilisateur.getTelephone())
                    .role(utilisateur.getRole())
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .emailVerifie(utilisateur.isEmailVerifie())
                    .adresse(utilisateur.getAdresse())
                    .ville(utilisateur.getVille())
                    .codePostal(utilisateur.getCodePostal())
                    .pays(utilisateur.getPays());

            // Ajouter les champs spécifiques aux producteurs
            if (utilisateur instanceof Producteur) {
                Producteur producteur = (Producteur) utilisateur;
                responseBuilder.nomExploitation(producteur.getNomExploitation())
                        .descriptionExploitation(producteur.getDescriptionExploitation())
                        .certifieBio(producteur.isCertifieBio())
                        .verifie(producteur.isVerifie());
            }

            return responseBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de rafraîchir le token : " + e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String token) {
        // Implémentation de la vérification de l'email
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailVerificationToken(token);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            utilisateur.setEmailVerifie(true);
            utilisateur.setEmailVerificationToken(null);
            utilisateurRepository.save(utilisateur);
        } else {
            throw new RuntimeException("Token de vérification invalide ou expiré");
        }
    }

    @Override
    public void sendVerificationEmail(String email) {
        // Implémentation de l'envoi d'email de vérification
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Vérification de votre compte AgriConnect");
        message.setText("Cliquez sur le lien suivant pour vérifier votre compte : " +
                "http://localhost:8080/api/v1/auth/verify?token=" + 
                jwtService.generateEmailVerificationToken(email));
        mailSender.send(message);
    }

    @Override
    public void resendVerificationEmail(String email) {
        // Implémentation de la renvoi d'email de vérification
        sendVerificationEmail(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implémentation de la réinitialisation du mot de passe
        // Ici vous devriez valider le token et mettre à jour le mot de passe
        throw new UnsupportedOperationException("Méthode non implémentée");
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        // Implémentation de l'envoi d'email pour réinitialiser le mot de passe
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de mot de passe - AgriConnect");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe : " +
                "http://localhost:8080/api/v1/auth/reset-password?token=" + 
                jwtService.generatePasswordResetToken(email));
        mailSender.send(message);
    }

    @Override
    public void logoutUser(String token) {
        // Implémentation de la déconnexion de l'utilisateur
        // Ici vous pourriez ajouter le token à une liste noire
    }

    @Override
    public boolean isEmailVerified(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        return utilisateurOpt.map(Utilisateur::isEmailVerifie).orElse(false);
    }
}
