package io.disposia.engine.exception;

public class EchoException extends Exception {

//    private static final Logger log = LoggerFactory.getLogger(EchoException.class);

    public EchoException() {

    }

    public EchoException(String message) {
        super(message);
    }

    public EchoException(final String message, final String errorConstant) {
        super(message);
    }

    public EchoException(String message, Throwable cause) {
        super(message, cause);
    }

    public EchoException(final String message, final String errorConstant, final Throwable cause) {
        super(message, cause);
    }

    public EchoException(Throwable cause) {
        super(cause);
    }

    public EchoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
