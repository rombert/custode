package nu.muntea.htr.storage.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;

import nu.muntea.htr.storage.api.Measurement;

public class Rrd4jStorageTest {
    
    private RrdBackendFactory factory;
    private final LocalCfg cfg = new LocalCfg();

    @BeforeEach
    void prepare() throws IOException {
        factory = RrdBackendFactory.getDefaultFactory();
        Files.deleteIfExists(Paths.get(cfg.location()));
    }

    @Test
    void averagesAreCalculated() throws IOException {
        Rrd4jStorage storage = new Rrd4jStorage(factory, cfg);
        Instant start = Instant.now();
        Instant current = start; // make the compiler happy
        for (int i = 0; i < 100; i++) {
            current = start.plus(i, ChronoUnit.SECONDS);
            storage.store(current, new Measurement(cfg.dataSourceNames()[0], 20));
        }

        try (RrdDb db = new RrdDb(storage.getDef().getPath(), false)) {
            FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, start.getEpochSecond(),
                    current.getEpochSecond());
            FetchData data = req.fetchData();

            int totalCount = data.getRowCount();

            long nanCount = Arrays.stream(data.getValues(0)).filter( d -> Double.isNaN(d)).count();
            
            assertEquals(100, totalCount, "Total entries");
            assertEquals(1, nanCount, "NaN entries"); // TODO - why do we still have 1?
        }
    }
    
    @Test
    void multipleRunsKeepOldData() throws IOException {

        Instant start = Instant.now();
        Instant current = start; // make the compiler happy

        for ( int i = 0 ; i < 2; i++ ) {
            Rrd4jStorage storage = new Rrd4jStorage(factory, cfg);
            for (int j = 0; j < 10; j++) {
                current = start.plus(i * 10 + j, ChronoUnit.SECONDS);
                storage.store(current, new Measurement(cfg.dataSourceNames()[0], 20));
            }
        }
        
        Rrd4jStorage storage = new Rrd4jStorage(factory, cfg);
        try (RrdDb db = new RrdDb(storage.getDef().getPath(), false)) {
            FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, start.getEpochSecond(),
                    current.getEpochSecond());
            FetchData data = req.fetchData();

            int totalCount = data.getRowCount();

            long nanCount = Arrays.stream(data.getValues(0)).filter( d -> Double.isNaN(d)).count();
            
            assertEquals(20, totalCount, "Total entries");
            assertEquals(1, nanCount, "NaN entries"); // TODO - why do we still have 1?
        }
    }
    
    static class LocalCfg implements Rrd4jStorage.Config {

        @Override
        public Class<? extends Annotation> annotationType() {
            return LocalCfg.class;
        }

        @Override
        public String[] dataSourceNames() {
            return new String[] { "cpu_temp" };
        }

        @Override
        public String location() {
            return "target/cpu_temp.rrd";
        }
        
        @Override
        public int stepSize() {
            return 1;
        }
        
    }
}
