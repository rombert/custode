package nu.muntea.htr.storage.rrd4j.internal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

// mostly useful for debugging
@Component( property = {
        "osgi.command.scope=htr",
        "osgi.command.function=render"
    }, service = RenderGraphCommand.class
)
public class RenderGraphCommand {

    
    private Storage storage;
    
    @Activate
    public RenderGraphCommand(@Reference Storage storage) {
        this.storage = storage;
    }
    
    public void render() throws FileNotFoundException, IOException {
        // TODO - parameters for output file, start and end
        try ( FileOutputStream fos = new FileOutputStream("target/temps.png")) {
            storage.renderGraph(Instant.now().minus(5, ChronoUnit.MINUTES), Instant.now(), fos);
        }
        
        System.out.println("Graph for last 5 minutes written to target/temps.png");
    }
}
