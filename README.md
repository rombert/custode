# Temperature tracking

Components:

- `client` - Arduino client code that sends temperature readings over HTTP
- `http` - HTTP endpoint that exposes graphs and accepts temperature readings
- `storage-api` - OSGi bundle that exposes API for storing temperature readings
- `storage-rrd4j` - OSGi bundle that implements the storage API via RRD4j
- `launcher` - Feature launcher that allows execution of the whole application


## Protocol

HTTP POST, parameters:

- timestamp: long, milliseconds since Epoch
- temp\_celsius: long, reading in degrees Celsius * 100
- source: String, indicator of location

## Launching

The `launcher` directory contains a `run.sh` helper script which can be used at
development time.

For production scenarios it's recommended to use the `daemon-run.sh` script
from the repository root, appending at least the additional configurations
to use, _e.g._:

    $ ./daemon-run.sh -a config-w541.json

The application status can then be queries using `systemd` commands:

    $ systemctl --user status htr
