package nu.muntea.custode.sensor.cpu_temp.internal;

import java.io.IOException;
import java.time.Instant;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import nu.muntea.custode.storage.api.Measurement;
import nu.muntea.custode.storage.api.Storage;
import nu.muntea.custode.storage.api.TemperatureReader;

@Component(immediate = true)
public class TemperatureRecorder {

    private static final long TIME_BETWEEN_READINGS = 1000l;  // TODO - configurable
    private static final String DATASOURCE_NAME = "cpu_temp"; // TODO - configurable
    
    private Thread thread;

    @Activate
    public TemperatureRecorder(
            @Reference(target="(dataSourceNames="+ DATASOURCE_NAME +")") Storage storage, 
            @Reference TemperatureReader reader
    ) {
        
        Runnable run = () -> {
            for ( ;; ) {
                try {
                    storage.store(Instant.now(), new Measurement(DATASOURCE_NAME, reader.readTemperature() * 100));
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
