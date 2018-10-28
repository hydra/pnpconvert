Examples
========

Example1 has board width of 70 and height of 100, PCB origin is the center.
When exported "Use PCB origin" was NOT selected, so the bottom left component has a position is closest to 0,0.
U1 is at the PCB origin but in the CSV file it's coordinates are relative to the bottom left component.


* Rotate example1 90 degrees

`pnpconvert -i examples/example1.csv -o example1.dpv -r 90 -rx 70 -ry 0 -c`


* Rotate example1 90 degrees and add 5mm rails

`pnpconvert -i examples/example1.csv -o example1.dpv -r 90 -rx 70 -ry 0 -oy 5 -ox 5 -c`