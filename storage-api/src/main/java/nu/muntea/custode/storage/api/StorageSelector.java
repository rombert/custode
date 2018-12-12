package nu.muntea.custode.storage.api;

import java.util.Optional;

public interface StorageSelector {

    Optional<Storage> select(String sourceName);
}
