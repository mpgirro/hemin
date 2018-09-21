package io.disposia.engine.exception;

/**
 * @author Maximilian Irro
 */
public class FeedParsingException extends EchoException {

    public FeedParsingException(String s, Exception e) {
        super(s,e);
    }
}
