PNPConvert
==========

by Dominic Clifton. (C) 2018

Utility to process DipTrace "Pick and Place" export files.

Basic Features
* Rotate board. Useful for boards panelled on the wrong axis.
* Add X/Y offsets. Useful to add space for rails that were added in programs other than DipTrace.
* Generates SVG images showing rotation and offset steps.

Usage
=====

pnpconvert -v

```
PNPConvert (C) 2018 Dominic Clifton
Written by Dominic Clifton
invalid parameter combinations
usage: pnpconvert
 -c                      convert
 -f <feeders>            feeders csv file
 -i <input>              input csv file
 -o <output>             output prefix
 -ox <offsetX>           X offset, applied after rotation
 -oy <offsetY>           Y offset, applied after rotation
 -r <rotation>           rotation degrees (positive is clockwise)
 -rx <rotationX>         rotation X origin
 -ry <rotationY>         rotation Y origin
 -s <source directory>   scan and import csv files
 -v                      version
```

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

DPV Generation process
======================

3 input files are required to generate a DPV file for your design.

Generate the DipTrace Pick and Place file
Use "File/Export/Pick and Place...".  Ensure the fields as per the example files in the `examples` folder.
The order of the CSV fields does not matter, the column headers are used to find the data.

1. Reads a DipTrace Pick and Place file
2. Any rotation and offset transformations are applied.
3. An updated Pick and Place file, with transformed coordinates and rotation angles is generated.
3. An SVG file is generated.
4. A feeders csv file which contains the list of feeders and pick settings.
5. A components csv file which contains component definitions.

The process starts by reading the pick-and-place file cross-referencing each component name and value against
 entries in the components file, then it looks for feeders that have the component.  When things match up the materials and components tables in the DPV file are generated.

When things don't match up a list of unknown componets and unloaded feeders is presented which should then be added to the appropriate input files.

SVG file
========

| Color | Usage |
| Red | Starting Coordinate |
| Blue | Coordinate after rotation |
| Pink | Coordinate after rotation and rotation coordinate offset |
| Green | Final coordinate after rotation, rotation coordinate offset and origin offset |

CSV files
=========

The order of the CSV fields does not matter in input files, the column headers are used to find the data.  This allows you to choose you preferred column order in your CSV editing tools/spreadsheets.
Column headers are case-insensitive, non-alphanumeric characters are converted to underscores before attempting a column header match.

Why
===
* The existing tools don't support DipTrace.
* The existing tools seem only to support the one EDA package they were designed for, I wanted a codebase that was more flexible to allow support for other packages to be added later.
* The existing tools and are not written in languages that I have tooling set-up for.
* The existing tools have no automated tests, I wan't to know key functionality is correct *before* I feed a file into my machine.
* The existing tools don't really support the CHMT48VB which has an additional feeder bank and some quirks regarding feeder placement.
* The existing tools do not internally use a consistent co-ordinate system.
* I needed something to rotate (not just flip) the co-ordinates and angles in the diptrace files.  Useful when you have board edge components and only 2 rails.
* I wanted to visualize the results of rotation and transform operations.

Technology Stack
================
* Java run-time environment - cross-platform.
* Groovy for production and test code.
* Spock for automated unit tests.
* Batik for SVG Generation.
* Gradle build tool.

Running Tests
=============

Running tests:

`gradle test`

Building
========

Use the gradle Application plugin's tasks

`gradle installDist`

then run the binaries in `build/install/pnpconvert`

e.g.

`./build/install/pnpconvert/bin/pnpconvert/pnpconvert -v`

Reference: https://docs.gradle.org/current/userguide/application_plugin.html

License
=======

GPLv3

