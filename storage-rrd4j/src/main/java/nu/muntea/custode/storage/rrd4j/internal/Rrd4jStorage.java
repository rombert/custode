package nu.muntea.custode.storage.rrd4j.internal;

import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;
import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import nu.muntea.custode.storage.api.Measurement;
import nu.muntea.custode.storage.api.Storage;

@Component(service = Storage.class, configurationPolicy = REQUIRE)
@Designate(factory = true, ocd = Rrd4jStorage.Config.class)
public class Rrd4jStorage implements Storage {
    
    @ObjectClassDefinition
    public @interface Config {
        
        @AttributeDefinition(name = "DataSource names")
        String[] dataSourceNames();
        
        @AttributeDefinition(name = "Location on filesystem")
        String location();
        
        @AttributeDefinition(name = "Step size in seconds")
        int stepSize();
    }

    private final RrdBackendFactory factory;
    private final String rrdPath;
    private final String[] dataSources;
    private final RrdDef rrdDef;

    @Activate
    public Rrd4jStorage(@Reference RrdBackendFactory factory, Config cfg) {
        
        this.factory = factory;
        dataSources = cfg.dataSourceNames();
        rrdPath = cfg.location();

        // TODO - step should be configurable (system-wide)
        // TODO - define more entires, and make configurable, e.g.
        // - x-step (1 second?) average for the last day
        // - y-step (1 minute?) average for the last month
        // - z-step (5 minutes?) average for the last 3 months?
        // others?
        rrdDef = new RrdDef(rrdPath, cfg.stepSize());
        rrdDef.addArchive(AVERAGE, 0.5, 1, 3600); // 1 second step * 3600 rows → records data for an hour
        rrdDef.addArchive(AVERAGE, 0.5, 10, 2160); // 10 second step * 2160 rows → records data for 6 hours
        rrdDef.addArchive(AVERAGE, 0.5, 60, 1440); // 60 second step * 1440 rows → records data for 1 day
        for ( String dataSource : dataSources )
            rrdDef.addDatasource(dataSource, DsType.GAUGE, 2 * rrdDef.getStep(), Double.NaN, Double.NaN);
        
        if ( Files.exists(Paths.get(rrdPath)) )
            return;

        // initialise with no data if needed
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
        
        // TODO - add a 'renderinghints' request class to contain width/height request
        // TODO - add a outputmedata class to allow passing back size, content type
        RrdGraphDef gDef = new RrdGraphDef();
        gDef.setAntiAliasing(true);
        gDef.setTextAntiAliasing(true);
        gDef.setTimeSpan(from.getEpochSecond(), to.getEpochSecond());
        gDef.setWidth(800);
        gDef.setHeight(300);
        gDef.setFilename("-");
        gDef.setUnitsExponent(0); // prevent scaling
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
