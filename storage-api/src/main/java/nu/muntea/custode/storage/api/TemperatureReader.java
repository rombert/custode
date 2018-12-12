package nu.muntea.custode.storage.api;

import java.io.IOException;

public interface TemperatureReader {

    // TODO - not sure InterruptedException makes sense in the API
    long readTemperature() throws IOException, InterruptedException;

}