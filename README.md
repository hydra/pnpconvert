PNPConvert
==========

Utility to process DipTrace "Pick and Place" export files.

Basic Features
* Rotate board. Useful for boards panelled on the wrong axis.
* Add X/Y offsets. Useful to add space for rails that were added in programs other than DipTrace.
* Generates SVG images showing rotation and offset steps.

Examples
========

Example1 has board width of 70 and height of 100, PCB origin is the center.
When exported "Use PCB origin" was NOT selected, so the bottom left component has a position is closest to 0,0.
U1 is at the PCB origin but in the CSV file it's coordinates are relative to the bottom left component.


* Rotate example1 90 degrees

`pnpconvert -i examples/example1.csv -o example1-90 -r 90 -rx 70 -ry 0 -c`


* Rotate example1 90 degrees and add 5mm rails

`pnpconvert -i examples/example1.csv -o example1-90-with-rails -r 90 -rx 70 -ry 0 -oy 5 -ox 5 -c`

* Rotate example1 270 degrees and add 5mm rails

`pnpconvert -i examples/example1.csv -o example1-270-with-rails -r 270 -rx 70 -ry 0 -ox 105 -oy 75 -c`

Note: PCB width and height has to be added to offsets to avoid negative component origins.