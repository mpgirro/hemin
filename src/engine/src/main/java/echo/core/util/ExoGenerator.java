package echo.core.util;

import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class ExoGenerator {

    private static final Logger log = LoggerFactory.getLogger(ExoGenerator.class);

    private final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Hashids hashids = new Hashids("Bit useless for my purpose, but why not", 0, ALPHABET);
    private final Integer shardId;
    private Integer seq;

    public ExoGenerator(int shardId) {
        this.shardId = shardId;
        this.seq = 0;
    }

    public synchronized String getNewExo() {
        final long now = System.currentTimeMillis();
        final String id = hashids.encode(seq, shardId, now);
        seq = (seq+1) % 1024;
        return id;
    }

}
