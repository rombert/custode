package nu.muntea.htr.storage.rrd4j.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        log("Starting up...");
        
    }

    public void stop(BundleContext context) throws Exception {
        log("Shutting down...");
    }
    
    private void log(String msg) {
        System.out.println(getClass().getName() + " " + msg);
    }
}
