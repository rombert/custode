# Temperature tracking

Components:

- `client` - Arduino client code that sends temperature readings over HTTP
- `server` - HTTP servlet that receives the temperature readings
- `storage-api` - OSGi bundle that exposes API for storing temperature readings
- `storage-rrd4j` - OSGi bundle that implements the storage API via RRD4j


## Protocol

HTTP POST, parameters:

- timestamp: long, milliseconds since Epoch
- temp\_celsius: long, reading in degrees Celsius * 100
- source: String, indicator of location
