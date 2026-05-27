package com.example.springmodulith.catalog.internal.exception;

public class AuthorNotFoundException extends RuntimeException {

    public AuthorNotFoundException(String message) {
        super(message);
    }

}
