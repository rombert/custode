package nu.muntea.htr.storage.rrd4j.internal;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

@Component(service = Storage.class)
public class Rrd4jStorage implements Storage {
    
    private final RrdBackendFactory factory;
    private String rrdPath = "target/temps.rrd"; // TODO - configurable
    private String[] dataSources = new String[] { "cpu_temp" }; // TODO - configurable
    private RrdDef rrdDef;

    @Activate
    public Rrd4jStorage(@Reference RrdBackendFactory factory) {
        
        this.factory = factory;

        rrdDef = new RrdDef(rrdPath, 1);
        rrdDef.addArchive(AVERAGE, 0.5, 1, 3600); // 1 second step * 3600 rows → records data for one hour
        for ( String dataSource : dataSources )
            rrdDef.addDatasource(dataSource, DsType.GAUGE, 2 * rrdDef.getStep(), Double.NaN, Double.NaN);
        
        // TODO - don't overwrite if exists
        // initialise with no data
        try {
            new RrdDb(rrdDef, factory).close();
        } catch (IOException e) {
            throw new RuntimeException("Failed creating RRD file", e);
        }
    }
    
    @Override
    public void store(Instant when, Measurement... measurements) {
        try (RrdDb rrdDb = new RrdDb(rrdDef.getPath(), factory)) {
            Sample sample = rrdDb.createSample(when.getEpochSecond());
            for ( Measurement measurement : measurements )
                sample.setValue(measurement.getName(), measurement.getDegreesCelsius() / 100.0);
            sample.update();
        } catch (IOException e) {
            throw new RuntimeException("Failed updating storage", e);
        }

    }
    
    @Override
    public void renderGraph(Instant from, Instant to, OutputStream out) {
        
        RrdGraphDef gDef = new RrdGraphDef();
        gDef.setTimeSpan(from.getEpochSecond(), to.getEpochSecond());
        gDef.setWidth(500);
        gDef.setHeight(300);
        gDef.setFilename("-");
        gDef.setTitle("Temperature");
        gDef.setVerticalLabel("Degrees Celsius");
        for ( String dataSource: dataSources ) {
            gDef.datasource(dataSource + "-average", rrdDef.getPath(), dataSource, AVERAGE, factory.getName());
            gDef.line(dataSource + "-average", Color.RED, "Temperature (" + dataSource +")");
        }
        gDef.setImageFormat("png");
        
        try {
            RrdGraph graph = new RrdGraph(gDef);
            out.write(graph.getRrdGraphInfo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed rendering graph", e);
        }
    }
    
    // visible for testing only
    RrdDef getDef() {
        return rrdDef;
    }

}
