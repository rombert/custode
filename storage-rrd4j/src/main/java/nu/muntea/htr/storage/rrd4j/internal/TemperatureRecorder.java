package nu.muntea.htr.storage.rrd4j.internal;

import java.io.IOException;
import java.time.Instant;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import nu.muntea.htr.storage.api.Measurement;
import nu.muntea.htr.storage.api.Storage;
import nu.muntea.htr.storage.api.TemperatureReader;

@Component(immediate = true)
public class TemperatureRecorder {

    private static final long TIME_BETWEEN_READINGS = 1000l;  // TODO - configurable
    
    private Thread thread;

    @Activate
    public TemperatureRecorder(@Reference Storage storage, @Reference TemperatureReader reader) {
        
        Runnable run = () -> {
            for ( ;; ) {
                try {
                    storage.store(Instant.now(), new Measurement("cpu_temp", reader.readTemperature()));
                    Thread.sleep(TIME_BETWEEN_READINGS);
                } catch (IOException e) {
                    throw new RuntimeException("Failed reading temperature data", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        
        thread = new Thread(run, getClass().getName());
        thread.start();
    }
    
    @Deactivate
    public void stop() throws InterruptedException {
        thread.interrupt();
        thread.join(2 * TIME_BETWEEN_READINGS); 
    }
}
