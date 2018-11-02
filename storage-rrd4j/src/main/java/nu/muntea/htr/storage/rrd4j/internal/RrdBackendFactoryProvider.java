package nu.muntea.htr.storage.rrd4j.internal;

import java.util.Hashtable;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdRandomAccessFileBackendFactory;

@Component
public class RrdBackendFactoryProvider {
    
    // TODO - possibly switch to setting the default factory -- has ordering problems!

    private ServiceRegistration<RrdBackendFactory> registration;

    @Activate
    public void start(ComponentContext ctx) {
        
        registration = ctx.getBundleContext().registerService(RrdBackendFactory.class, new RrdRandomAccessFileBackendFactory(), new Hashtable<>());
    }
    
    @Deactivate
    public void deactivate() {
        if ( registration != null )
            registration.unregister();
        
    }
}
