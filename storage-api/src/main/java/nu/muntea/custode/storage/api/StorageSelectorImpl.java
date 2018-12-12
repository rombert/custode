package nu.muntea.custode.storage.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class StorageSelectorImpl implements StorageSelector {

    private final List<Map.Entry<Map<String, ?>, Storage>> storages;

    @Activate
    public StorageSelectorImpl(@Reference List<Map.Entry<Map<String, ?>, Storage>> storages) {
        this.storages = storages;
    }
    
    @Override
    public Optional<Storage> select(String sourceName) {
        for ( Map.Entry<Map<String, ?>, Storage> entry : storages ) {
            Object prop = entry.getKey().get("dataSourceNames");
            if ( !(prop instanceof String[]) ) 
                continue;
            
            for ( String dataSourceNames : ((String[]) prop) )
                if ( sourceName.equals(dataSourceNames) )
                    return Optional.of(entry.getValue());
        }
        
        return Optional.empty();
    }

}
