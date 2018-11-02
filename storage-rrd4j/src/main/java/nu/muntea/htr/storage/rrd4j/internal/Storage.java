package nu.muntea.htr.storage.rrd4j.internal;

import java.io.OutputStream;
import java.time.Instant;

public interface Storage {
    
    void store(Instant when, Measurement... measurements);

    void renderGraph(Instant from, Instant to, OutputStream out);

}
