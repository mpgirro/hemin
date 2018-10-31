package io.hemin.engine.exception;

public class HeminException extends Exception {

    public HeminException() { }

    public HeminException(String message) {
        super(message);
    }

    public HeminException(String message, String errorConstant) {
        super(message);
    }

    public HeminException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeminException(String message, String errorConstant, Throwable cause) {
        super(message, cause);
    }

    public HeminException(Throwable cause) {
        super(cause);
    }

    public HeminException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
