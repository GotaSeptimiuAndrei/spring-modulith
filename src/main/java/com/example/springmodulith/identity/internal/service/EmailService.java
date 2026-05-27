package com.example.springmodulith.identity.internal.service;

import com.example.springmodulith.catalog.AuthorRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.example.springmodulith.identity.RegistrationType;
import com.example.springmodulith.identity.User;
import com.example.springmodulith.identity.internal.dto.EmailVerificationRequest;
import com.example.springmodulith.identity.internal.utils.UserConverter;
import com.example.springmodulith.identity.internal.dto.UserSignupRequest;
import com.example.springmodulith.identity.internal.model.EmailVerification;
import com.example.springmodulith.identity.internal.repository.EmailVerificationRepository;
import com.example.springmodulith.identity.internal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final EmailVerificationRepository verificationRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Booknest! Here is your email verification code");
        message.setText("Use this code to complete your registration: " + verificationCode);
        mailSender.send(message);
    }

    /**
     * Verifies a code, then persists the pending User or Author that was serialized in
     * EmailVerification.registrationPayload
     */
    @Transactional
    public void verifyAndPersistAccount(EmailVerificationRequest req) {

        EmailVerification v = verificationRepository
                .findByEmailAndVerificationCodeAndVerifiedFalse(req.getEmail(), req.getVerificationCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid code or already verified"));

        if (v.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Verification code expired");

        try {
            if (v.getRegistrationType() == RegistrationType.USER) {
                UserSignupRequest dto = objectMapper.readValue(v.getRegistrationPayload(), UserSignupRequest.class);
                User user = UserConverter.convertToEntity(dto);
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
            }
            else {
                // Parse the JSON payload we stored in UserService
                com.fasterxml.jackson.databind.JsonNode payload = objectMapper.readTree(v.getRegistrationPayload());

                // Generate a mock avatar URL using the author's name
                String mockImageUrl = "https://ui-avatars.com/api/?name=" +
                        payload.get("name").asText().replace(" ", "+") + "&background=random";

                // Safely encode the password here before sending it to the catalog
                String encodedPassword = passwordEncoder.encode(payload.get("password").asText());

                // Publish the event to decouple the modules
                eventPublisher.publishEvent(new AuthorRegisteredEvent(
                        payload.get("name").asText(),
                        v.getEmail(),
                        encodedPassword,
                        java.time.LocalDate.parse(payload.get("birthDate").asText()),
                        payload.get("city").asText(),
                        payload.get("country").asText(),
                        payload.get("bio").asText(),
                        mockImageUrl
                ));
            }

            v.setVerified(true);
            verificationRepository.save(v);

        }
        catch (Exception ex) {
            throw new IllegalStateException("Corrupted verification payload", ex);
        }
    }

}
