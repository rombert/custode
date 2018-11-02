package nu.muntea.htr.storage.rrd4j.internal;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;

public class Rrd4jStorageTest {

    @Test
    void averagesAreCalculated() throws IOException {
        Rrd4jStorage storage = new Rrd4jStorage(RrdBackendFactory.getDefaultFactory());
        Instant start = Instant.now();
        Instant current = start; // make the compiler happy
        for (int i = 0; i < 1200; i++) {
            current = start.plus(i, ChronoUnit.SECONDS);
            storage.store(current, new Measurement("cpu_temp", 20));
        }

        try (RrdDb db = new RrdDb(storage.getDef().getPath(), false)) {
            FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, start.getEpochSecond(),
                    current.getEpochSecond());
            FetchData data = req.fetchData();

            int totalCount = data.getRowCount();

            long nanCount = Arrays.stream(data.getValues(0)).filter( d -> Double.isNaN(d)).count();
            
            assertEquals(1200, totalCount, "Total entries");
            assertEquals(1, nanCount, "NaN entries"); // TODO - why do we still have 1?
        }

    }
}
