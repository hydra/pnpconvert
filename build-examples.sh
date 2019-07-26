#!/bin/sh

PNPCONVERT=./build/install/pnpconvert/bin/pnpconvert

$PNPCONVERT -i examples/example1.csv -o examples/example1-90-with-rails -f examples/feeders.csv -co examples/components.csv -t examples/trays.csv -r 90 -rx 70 -ry 0 -ox 5 -oy 5 -c
$PNPCONVERT -i examples/example1.csv -o examples/example1-270-with-rails -f examples/feeders.csv -co examples/components.csv -t examples/trays.csv -r 270 -rx 70 -ry 0 -ox 105 -oy 75 -c
$PNPCONVERT @examples/example1-job1.pnpconvert

