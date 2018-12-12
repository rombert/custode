package nu.muntea.custode.log.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        SLF4JBridgeHandler.uninstall();
    }
}
