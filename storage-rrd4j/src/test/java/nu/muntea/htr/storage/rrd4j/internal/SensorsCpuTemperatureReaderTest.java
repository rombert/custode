package nu.muntea.htr.storage.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Test;

public class SensorsCpuTemperatureReaderTest {
    
    @Test
    public void readCpuData() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader(new LocalCfg()) {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(0, getClass().getResourceAsStream("/w541_sensors_output.json"));
            }
        };
        
        assertEquals(75, reader.readTemperature());
    }

    @Test
    
    public void errorExitCode() throws IOException, InterruptedException {
        
        TemperatureReader reader = new SensorsCpuTemperatureReader(new LocalCfg()) {
            @Override
            protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
                return new ExecutionResult(1, new ByteArrayInputStream(new byte[0]));
            }
        };
        
        assertThrows(RuntimeException.class, () -> reader.readTemperature());
    }

    static class LocalCfg implements SensorsCpuTemperatureReader.Config {

        @Override
        public Class<? extends Annotation> annotationType() {
            return SensorsCpuTemperatureReader.Config.class;
        }

        @Override
        public String sensors_json_path() {
            return "coretemp-isa-0000/Package id 0/temp1_input";
        }
        
    }
}
