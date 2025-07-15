package com.cybernerd.agriConnect_APIBackend.serviceImpl.authserviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import com.cybernerd.agriConnect_APIBackend.security.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JavaMailSender mailSender;
    private UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl( 
        JwtService jwtService, 
        UserDetailsService userDetailsService,
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
        // Créer un acheteur basique
        Acheteur acheteur = new Acheteur();
        acheteur.setNom(request.getNom());
        acheteur.setEmail(request.getEmail());
        acheteur.setMotDePasse(motDePasseEncode);
        acheteur.setRole(com.cybernerd.agriConnect_APIBackend.enumType.Role.ACHETEUR);
        acheteur.setTelephone(request.getTelephone());
        acheteur.setEmailVerifie(false);
        acheteur.setFromGoogle(false);
        // Générer un code de vérification à 6 chiffres
        String codeVerification = String.format("%06d", (int)(Math.random() * 1000000));
        acheteur.setCodeVerificationEmail(codeVerification);
        acheteur.setCodeVerificationExpiration(java.time.LocalDateTime.now().plusHours(24));
        // Enregistrer l'utilisateur
        utilisateurRepository.save(acheteur);
        // Envoyer l'email de vérification avec le code
        sendVerificationEmail(acheteur.getEmail(), codeVerification);
        // Retourner la réponse
        return AuthResponse.builder()
                .id(acheteur.getId())
                .nom(acheteur.getNom())
                .email(acheteur.getEmail())
                .telephone(acheteur.getTelephone())
                .role(acheteur.getRole())
                .emailVerifie(acheteur.isEmailVerifie())
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
        // String refreshToken = jwtService.generateRefreshToken(userDetails); // supprimé

        // Construire la réponse
        AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .token(jwt)
                .refreshToken("") // refreshToken non géré
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
            // String newRefreshToken = jwtService.generateRefreshToken(userDetails); // supprimé

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
                    .refreshToken("") // refreshToken non géré
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
    public void verifyEmail(String code) {
        // Vérification du code à 6 chiffres
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByCodeVerificationEmail(code);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.getCodeVerificationExpiration() == null || utilisateur.getCodeVerificationExpiration().isBefore(java.time.LocalDateTime.now())) {
                throw new RuntimeException("Code de vérification expiré, veuillez demander un nouveau code.");
            }
            utilisateur.setEmailVerifie(true);
            utilisateur.setCodeVerificationEmail(null);
            utilisateur.setCodeVerificationExpiration(null);
            utilisateurRepository.save(utilisateur);
        } else {
            throw new RuntimeException("Code de vérification invalide ou expiré");
        }
    }

    @Override
    public void sendVerificationEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Vérification de votre compte AgriConnect");
        message.setText("Votre code de vérification est : " + code);
        mailSender.send(message);
    }

    @Override
    public void resendVerificationEmail(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            String codeVerification = String.format("%06d", (int)(Math.random() * 1000000));
            utilisateur.setCodeVerificationEmail(codeVerification);
            utilisateur.setCodeVerificationExpiration(java.time.LocalDateTime.now().plusHours(24));
            utilisateurRepository.save(utilisateur);
            sendVerificationEmail(email, codeVerification);
        } else {
            throw new RuntimeException("Utilisateur non trouvé");
        }
    }


    @Override
    public void sendPasswordResetEmail(String email) {
        // Implémentation de l'envoi d'email pour réinitialiser le mot de passe
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de mot de passe - AgriConnect");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe : " +
                "http://localhost:8080/api/v1/auth/reset-password?token=" + 
                "TOKEN_A_GENERER" // jwtService.generatePasswordResetToken(email) supprimé
        );
        mailSender.send(message);
    }

    @Override
    public boolean isEmailVerified(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        return utilisateurOpt.map(Utilisateur::isEmailVerifie).orElse(false);
    }
}
