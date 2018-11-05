package nu.muntea.htr.storage.rrd4j.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = TemperatureReader.class, 
    configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SensorsCpuTemperatureReader.Config.class)
public class SensorsCpuTemperatureReader implements TemperatureReader {
   
    @ObjectClassDefinition
    public @interface Config {
        
        @AttributeDefinition
        String sensors_json_path();
    }

    private final String path;
    
    @Activate
    public SensorsCpuTemperatureReader(Config cfg) {
        path = cfg.sensors_json_path();
    }

    @Override
    public long readTemperature() throws IOException, InterruptedException {
        
        ExecutionResult res = runSensorsCommand();
        
        if ( res.exitCode!= 0 ) {
            List<String> lines = consumeOutput(res.in);
            throw new RuntimeException("Measurement failed, exit code " + res.exitCode + "\n" + lines.toString());
        }
        
        try ( JsonReader reader = Json.createReader(res.in) ) {
            JsonObject object = reader.readObject();
            String[] segments = path.split("/");
            for ( int i = 0 ; i < segments.length; i++ ) {
                if ( i < segments.length -1 )
                    object = object.getJsonObject(segments[i]);
                else
                    return object.getJsonNumber(segments[i]).longValue();
            }
        }
        
        throw new AssertionError("Never happens");
    }

    // visible for testing
    protected ExecutionResult runSensorsCommand() throws IOException, InterruptedException {
        
        ProcessBuilder pb = new ProcessBuilder("sensors", "-j");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.waitFor();
        
        return new ExecutionResult(proc.waitFor(), proc.getInputStream());
    }

    private List<String> consumeOutput(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        try ( InputStreamReader r = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(r)) {
            String line;
            while ((line = br.readLine()) != null)
                lines.add(line);
        }
        return lines;
    }
    
    static class ExecutionResult {
        int exitCode;
        InputStream in;
        
        ExecutionResult(int exitCode, InputStream in) {
            this.exitCode = exitCode;
            this.in = in;
        }
        
        
    }
}
