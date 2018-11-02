package nu.muntea.htr.storage.rrd4j.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

@Component(service = TemperatureReader.class)
public class SensorsCpuTemperatureReader implements TemperatureReader {
    
    /* (non-Javadoc)
     * @see nu.muntea.htr.storage.rrd4j.internal.TemperatureReader#readTemperature()
     */
    @Override
    public long readTemperature() throws IOException, InterruptedException {
        
        // TODO - JSON structure to read from should be configurable, JSONPath via Johnzon possible?
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "sensors -j | jq '.\"coretemp-isa-0000\".\"Package id 0\".temp1_input'");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        int ret = proc.waitFor();
        List<String> lines = consumeOutput(proc);
        if ( ret != 0 ) {
            throw new RuntimeException("Measurement failed, exit code " + ret + "\n" + lines.toString());
        }
        
        return Long.parseLong(lines.get(0));
    }

    private List<String> consumeOutput(Process proc) throws IOException {
        List<String> lines = new ArrayList<>();
        try ( InputStreamReader r = new InputStreamReader(proc.getInputStream());
                BufferedReader br = new BufferedReader(r)) {
            String line;
            while ((line = br.readLine()) != null)
                lines.add(line);
        }
        return lines;
    }
}
