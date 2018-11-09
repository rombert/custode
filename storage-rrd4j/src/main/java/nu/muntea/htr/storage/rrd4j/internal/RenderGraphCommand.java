package nu.muntea.htr.storage.rrd4j.internal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import nu.muntea.htr.storage.api.Storage;
import nu.muntea.htr.storage.api.StorageSelector;

// mostly useful for debugging
@Component( property = {
        "osgi.command.scope=htr",
        "osgi.command.function=render"
    }, service = RenderGraphCommand.class
)
public class RenderGraphCommand {

    
    private StorageSelector selector;
    
    @Activate
    public RenderGraphCommand(@Reference StorageSelector selector) {
        this.selector = selector;
    }
    
    public void render(String sourceName, int minutes) throws FileNotFoundException, IOException {
        
        Storage storage = selector.select(sourceName).orElseThrow(() -> new RuntimeException("No dataSource with name '" + sourceName + "' found"));
        
        try ( FileOutputStream fos = new FileOutputStream("target/" + sourceName + ".png")) {
            storage.renderGraph(Instant.now().minus(minutes, ChronoUnit.MINUTES), Instant.now(), fos);
        }
        
        System.out.println("Graph for last " + minutes + " minutes and data source " + sourceName + " written to target/" + sourceName + ".png");
    }
}
