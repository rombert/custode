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

## Accessing the graphs

### Gogo shell

If the _shell.json_ feature is included, a gogo command can be used to write a
PNG image of the recorded temperatures.

    g! htr:render 20

The above command writes a PNG with the last 20 minutes of recorded temperatures.

### HTTP endpoint

An HTTP service is started on port 8101 and exposes a graph endpoint at /graph.png .
By default it exposes the last 5 minutes of graphs. The _source_ paramter is required.
Additionally, the interval can be tweaked by the _minutesAgo_ parameter. For example:

* http://localhost:8101/graph.png?source=cpu_temp - displays data for the last 5 minutes
* http://localhost:8101/graph.png?source\_cpu\_temp&minutesAgo=60 - displays data for the last hour
