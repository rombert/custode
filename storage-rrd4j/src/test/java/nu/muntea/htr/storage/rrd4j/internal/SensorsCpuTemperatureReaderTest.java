package nu.muntea.htr.storage.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.osgi.util.converter.Converters;

import nu.muntea.htr.storage.api.TemperatureReader;
import nu.muntea.htr.storage.rrd4j.internal.SensorsCpuTemperatureReader.Config;

public class SensorsCpuTemperatureReaderTest {
    
    private final Config cfg = 
            Converters.standardConverter().convert(Collections.singletonMap("sensors_json_path", "coretemp-isa-0000/Package id 0/temp1_input")).to(Config.class);
    
    @Test
    public void readCpuData() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader(cfg) {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(0, getClass().getResourceAsStream("/w541_sensors_output.json"));
            }
        };
        
        assertEquals(75, reader.readTemperature());
    }

    @Test
    
    public void errorExitCode() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader(cfg) {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(1, new ByteArrayInputStream(new byte[0]));
            }
        };
        
        assertThrows(RuntimeException.class, () -> reader.readTemperature());
    }
}
