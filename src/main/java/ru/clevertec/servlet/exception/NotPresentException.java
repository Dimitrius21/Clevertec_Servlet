package ru.clevertec.servlet.exception;

public class NotPresentException extends RuntimeException{
    public NotPresentException() {
    }

    public NotPresentException(String message) {
        super(message);
    }
}
