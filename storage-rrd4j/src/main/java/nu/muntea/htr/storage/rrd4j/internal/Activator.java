package nu.muntea.htr.storage.rrd4j.internal;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdRandomAccessFileBackendFactory;

public class Activator implements BundleActivator {
    
    public static void main(String[] args) throws Exception {
        new Activator().start(null);
    }

    public void start(BundleContext context) throws Exception {
        log("Starting up...");

        RrdBackendFactory backendFactory = new RrdRandomAccessFileBackendFactory();

        Storage storage = new Rrd4jStorage(backendFactory);

        Instant start = Instant.now();
        Instant end = start;
        for ( int i = 0 ; i < 120; i++) {
            end = Instant.now();
            storage.store(end, getCpuTemp());
            Thread.sleep(1000l);
        }
        
        try ( FileOutputStream out = new FileOutputStream("target/temps.png")) {
            log("Rendering temps from " + start + " to " + end);
            storage.renderGraph(start, end, out);
        }
    }

    public void stop(BundleContext context) throws Exception {
        log("Shutting down...");
    }

    private void log(String msg) {
        System.out.println(getClass().getName() + " " + msg);
    }
    
    private Measurement getCpuTemp() throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "sensors -j | jq '.\"coretemp-isa-0000\".\"Package id 0\".temp1_input'");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        int ret = proc.waitFor();
        List<String> lines = consumeOutput(proc);
        if ( ret != 0 ) {
            throw new RuntimeException("Measurement failed, exit code " + ret + "\n" + lines.toString());
        }
        
        long parseLong = Long.parseLong(lines.get(0));
        
        log("Read cpu_temp : " + parseLong);
        
        return new Measurement("cpu_temp", parseLong * 100);
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
