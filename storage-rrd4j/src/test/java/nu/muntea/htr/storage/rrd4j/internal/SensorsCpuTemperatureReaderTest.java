package nu.muntea.htr.storage.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class SensorsCpuTemperatureReaderTest {
    
    @Test
    public void readCpuData() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader() {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(0, getClass().getResourceAsStream("/w541_sensors_output.json"));
            }
        };
        
        assertEquals(75, reader.readTemperature());
    }

    @Test
    
    public void errorExitCode() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader() {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(1, new ByteArrayInputStream(new byte[0]));
            }
        };
        
        assertThrows(RuntimeException.class, () -> reader.readTemperature());
    }

}
