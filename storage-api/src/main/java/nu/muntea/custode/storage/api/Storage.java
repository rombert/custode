package nu.muntea.custode.storage.api;

import java.io.OutputStream;
import java.time.Instant;

public interface Storage {
    
    void store(Instant when, Measurement... measurements);

    void renderGraph(Instant from, Instant to, OutputStream out);

}
