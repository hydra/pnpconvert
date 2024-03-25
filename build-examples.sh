#!/bin/sh

PNPCONVERT=./build/install/pnpconvert/bin/pnpconvert

$PNPCONVERT -i examples/example1.csv -o examples/example1-90-with-rails -f examples/feeders.csv -co examples/components.csv -t examples/trays.csv -r 90 -bblox -35 -bbloy -50 -bw 70 -bh 100 -ox 5 -oy 5 -c -st
$PNPCONVERT -i examples/example1.csv -o examples/example1-270-with-rails -f examples/feeders.csv -co examples/components.csv -t examples/trays.csv -r 270 -bblox -35 -bbloy -50 -bw 70 -bh 100 -ox 105 -oy 75 -c -st
$PNPCONVERT @examples/example1-job1.pnpconvert

$PNPCONVERT -ft -o examples/feeders -f examples/feeders.csv -co examples/components.csv -t examples/trays.csv

