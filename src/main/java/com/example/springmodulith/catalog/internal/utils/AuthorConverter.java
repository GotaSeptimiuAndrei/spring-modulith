package com.example.springmodulith.catalog.internal.utils;

import com.example.springmodulith.catalog.Author;
import com.example.springmodulith.catalog.internal.dto.AuthorResponse;

public class AuthorConverter {

    public static AuthorResponse convertToDto(Author author) {
        AuthorResponse dto = new AuthorResponse();
        dto.setAuthorId(author.getAuthorId());
        dto.setFullName(author.getFullName());
        dto.setEmail(author.getEmail());
        dto.setDateOfBirth(author.getDateOfBirth());
        dto.setCity(author.getCity());
        dto.setCountry(author.getCountry());
        dto.setBio(author.getBio());
        dto.setPhoto(author.getPhoto());
        return dto;
    }

}
