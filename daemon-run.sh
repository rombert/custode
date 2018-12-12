#!/bin/sh

systemd-run --user --property=WorkingDirectory=$(pwd)/launcher --unit=custode ./launcher/run.sh "$@"
