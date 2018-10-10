package io.hemin.engine.exception;

public class HeminException extends Exception {

//    private static final Logger log = LoggerFactory.getLogger(HeminException.class);

    public HeminException() {

    }

    public HeminException(String message) {
        super(message);
    }

    public HeminException(final String message, final String errorConstant) {
        super(message);
    }

    public HeminException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeminException(final String message, final String errorConstant, final Throwable cause) {
        super(message, cause);
    }

    public HeminException(Throwable cause) {
        super(cause);
    }

    public HeminException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
