package nu.muntea.custode.storage.rrd4j.internal;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;

import nu.muntea.custode.storage.api.Storage;
import nu.muntea.custode.storage.api.StorageSelector;

//mostly useful for debugging
@Component( property = {
     "osgi.command.scope=custode",
     "osgi.command.function=dump"
 }, service = DumpEntriesCommand.class
)
public class DumpEntriesCommand {

    private StorageSelector selector;
    private RrdBackendFactory factory;
    
    @Activate
    public DumpEntriesCommand(@Reference StorageSelector selector, 
            @Reference RrdBackendFactory factory) {
        this.selector = selector;
        this.factory = factory;
    }
    
    public void dump(String dataSourceName) throws IOException {
        
        Optional<Storage> select = selector.select(dataSourceName);
        Storage storage = select
            .orElseThrow(() -> new RuntimeException("New dataSource with name " + dataSourceName));
        if ( !(storage instanceof Rrd4jStorage) )
            throw new RuntimeException("Data source is not a rrd4j one");
        
        RrdDef def = ((Rrd4jStorage) storage).getDef();
        
        System.out.println("Rendering data for the last 5 minutes");
        
        try {
            try ( RrdDb db = new RrdDb(def.getPath(), true, factory ) ) {
                FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, 
                        Instant.now().minus(5, ChronoUnit.SECONDS).getEpochSecond(),
                        Instant.now().getEpochSecond());
                
                FetchData data = req.fetchData();
                for ( double row[] : data.getValues() )
                    System.out.println(Arrays.toString(row));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
