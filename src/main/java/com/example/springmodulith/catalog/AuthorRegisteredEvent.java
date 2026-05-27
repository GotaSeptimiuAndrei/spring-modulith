package com.example.springmodulith.catalog;

import java.time.LocalDate;

public record AuthorRegisteredEvent(
        String name,
        String email,
        String encodedPassword,
        LocalDate birthDate,
        String city,
        String country,
        String bio,
        String photo
) {}