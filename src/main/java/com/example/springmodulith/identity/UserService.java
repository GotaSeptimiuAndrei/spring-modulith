package com.example.springmodulith.identity;

import com.example.springmodulith.identity.internal.dto.AdminDTO;
import com.example.springmodulith.identity.internal.dto.AuthorSignupRequest;
import com.example.springmodulith.identity.internal.dto.UserSignupRequest;
import com.example.springmodulith.identity.internal.model.EmailVerification;
import com.example.springmodulith.identity.internal.repository.EmailVerificationRepository;
import com.example.springmodulith.identity.internal.repository.UserRepository;
import com.example.springmodulith.identity.internal.security.VerificationCodeGenerator;
import com.example.springmodulith.identity.internal.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationRepository emailVerificationRepository;

    private final EmailService emailService;

    public void registerUser(UserSignupRequest dto) throws JsonProcessingException {
        if (userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        if (emailVerificationRepository.existsByEmailAndVerifiedFalse(dto.getEmail()))
            throw new IllegalArgumentException("A verification code was already sent to this email");

        String code = VerificationCodeGenerator.generateVerificationCode();

        EmailVerification v = new EmailVerification();
        v.setEmail(dto.getEmail());
        v.setVerificationCode(code);
        v.setCreatedAt(LocalDateTime.now());
        v.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        v.setRegistrationType(RegistrationType.USER);
        v.setRegistrationPayload(new ObjectMapper().writeValueAsString(dto));
        emailVerificationRepository.save(v);

        emailService.sendVerificationEmail(dto.getEmail(), code);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void registerAuthor(AuthorSignupRequest dto) throws JsonProcessingException {
        if (userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        if (emailVerificationRepository.existsByEmailAndVerifiedFalse(dto.getEmail()))
            throw new IllegalArgumentException("A verification code was already sent to this email");

        String code = VerificationCodeGenerator.generateVerificationCode();

        EmailVerification v = new EmailVerification();
        v.setEmail(dto.getEmail());
        v.setVerificationCode(code);
        v.setCreatedAt(LocalDateTime.now());
        v.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        v.setRegistrationType(RegistrationType.AUTHOR);

        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("name", dto.getFullName());
        payload.put("email", dto.getEmail());
        payload.put("password", dto.getPassword());
        payload.put("birthDate", dto.getDateOfBirth().toString());
        payload.put("city", dto.getCity());
        payload.put("country", dto.getCountry());
        payload.put("bio", dto.getBio());

        v.setRegistrationPayload(new ObjectMapper().writeValueAsString(payload));
        emailVerificationRepository.save(v);

        emailService.sendVerificationEmail(dto.getEmail(), code);
    }

    public User createAdmin(AdminDTO dto) {
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        });

        User admin = new User();
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setUsername(dto.getUsername());
        admin.setIsAdmin(true);

        return userRepository.save(admin);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
