package com.example.springmodulith.catalog;

import com.example.springmodulith.catalog.internal.dto.AuthorResponse;
import com.example.springmodulith.catalog.internal.exception.AuthorNotFoundException;
import com.example.springmodulith.catalog.internal.repository.AuthorRepository;
import com.example.springmodulith.catalog.internal.utils.AuthorConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;

    @ApplicationModuleListener
    public void onAuthorRegistered(AuthorRegisteredEvent event) {
        Author author = new Author();
        author.setFullName(event.name());
        author.setEmail(event.email());
        author.setPassword(event.encodedPassword());
        author.setDateOfBirth(event.birthDate());
        author.setCity(event.city());
        author.setCountry(event.country());
        author.setBio(event.bio());
        author.setPhoto(event.photo());

        authorRepository.save(author);
        log.info("Catalog caught event! Saved Author: {}", event.name());
    }

    public Optional<Author> findByEmail(String email) {
        return authorRepository.findByEmail(email);
    }

    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    public Page<AuthorResponse> getAllAuthorsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Author> authorsPage = authorRepository.findAll(pageable);
        return authorsPage.map(AuthorConverter::convertToDto);
    }

    public AuthorResponse getAuthorByFullName(String fullName) {
        Author author = authorRepository.findByFullNameIgnoreCase(fullName)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with full name: " + fullName));
        return AuthorConverter.convertToDto(author);
    }

    public Page<AuthorResponse> searchAuthorsByName(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Author> authorsPage = authorRepository.findByFullNameIgnoreCaseContaining(query, pageable);
        return authorsPage.map(AuthorConverter::convertToDto);
    }

}
