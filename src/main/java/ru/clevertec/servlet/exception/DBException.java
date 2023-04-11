package ru.clevertec.servlet.exception;

public class DBException extends RuntimeException{
    public DBException() {
    }

    public DBException(Throwable cause) {
        super(cause);
    }

    public DBException(String message) {
        super(message);
    }
    public DBException(String message, Exception ex) {
        super(message, ex);
    }
}
