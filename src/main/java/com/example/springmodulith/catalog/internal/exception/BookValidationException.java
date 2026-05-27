package com.example.springmodulith.catalog.internal.exception;

public class BookValidationException extends RuntimeException {

    public BookValidationException(String message) {
        super(message);
    }

}