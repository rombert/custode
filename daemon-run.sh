#!/bin/sh

systemd-run --user --property=WorkingDirectory=$(pwd)/launcher --unit=htr ./launcher/run.sh "$@"
