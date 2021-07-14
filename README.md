PNPConvert
==========

by Dominic Clifton. (C) 2018-2020

[![Build Status](https://travis-ci.com/hydra/pnpconvert.svg?branch=master)](https://travis-ci.com/hydra/pnpconvert)

Utility to process DipTrace "Pick and Place" export files and generate DPV format files suitable for use with CharmHigh
Technology pick-and-place SMT machines.

Basic Features
* Rotate board. Useful for boards panelled on the wrong axis.
* Add X/Y offsets. Useful to add space for rails that were added in programs other than DipTrace.
* Generates SVG images showing rotation and offset steps.

Currently supported machines

* CHMT48VB
* CHMT48VA (*1)
* CHMT36VA (*1)

*1 Treated as CHMT48VB, so do not use right-hand side feeders with IDs 36-64.

Usage
=====

`pnpconvert <args>`

```
PNPConvert (C) 2018 Dominic Clifton
Written by Dominic Clifton
Usage: pnpconvert
  -c                        convert
      -cfg=<config>         configuration file (in "key=value" format)
      -co=<components>      components csv file/url
      -dr=<disableRefdes>   Disable components by refdes (comma separated list)
  -f=<feeders>              feeders csv file/url
      -fm=<fiducialMarkers>...
                            Fiducial marker list (note,x,y[ ...])
      -ft                   Generate DPV containing all feeders
  -i=<input>                input csv file/url
  -m=<mirroring>            mirroring mode (horizontal/vertical/both/none),
                              default is none
      -mx=<mirroringX>      mirroring X origin
      -my=<mirroringY>      mirroring Y origin
  -o=<output>               output prefix
      -ox=<offsetX>         X offset, applied after rotation
      -oy=<offsetY>         Y offset, applied after rotation
      -oz=<offset>          Z offset, applied to all component heights -
                              increase for thicker PCBs
      -pix=<panelIntervalX> Interval spacing on the X axis
      -piy=<panelIntervalY> Interval spacing on the Y axis
      -pnx=<panelNumberX>   Number of PCBs on the X axis
      -pny=<panelNumberY>   Number of PCBs on the Y axis
  -r=<rotation>             rotation degrees (positive is clockwise)
      -rr=<replaceRefdes>...
                            Replace components by refdes ("refdes,value,name"[
                              ...])
      -rx=<rotationX>       rotation X origin
      -ry=<rotationY>       rotation Y origin
  -s=<side>                 pcb side (top|bottom|all), default is all
  -t=<trays>                trays csv file/url
  -v                        version
```

PnPConvert also supports reading one or more files containing arguments, prefix each filename with an @ symbol.

Arguments are processed in the order they appear.  If an argument is specified more than once, the first occurrence applies.

```
pnpconvert @args-file1 @args-file2
```

Configuration values for the specific input,output,feeders,components and tray settings can be loaded from a config file in `key=value` format, e.g.

Example config file
```
feeders=https://docs.google.com/spreadsheet/ccc?key=1-bZiPxQy2budCd0ny81PV6aGKu4q8ckkRBx3FWsMj-M&gid=0&output=csv
components=https://docs.google.com/spreadsheet/ccc?key=1-bZiPxQy2budCd0ny81PV6aGKu4q8ckkRBx3FWsMj-M&gid=783842964&output=csv
trays=https://docs.google.com/spreadsheet/ccc?key=1-bZiPxQy2budCd0ny81PV6aGKu4q8ckkRBx3FWsMj-M&gid=433487055&output=csv
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

* Apply a 0.5mm Z offset to when placing parts.

`pnpconvert ... -z 0.5`

Note for the CHMT machines: Using a Z offset simply adds the offset to the height of each component, so that when the
head places the component it does't push it down so far, this can help if your PCBs flex when placing parts.  Ideally
any good machine should have a setting for PCB thickness...

* Skip placement of some components

`pnpconvert ... -dr J1,J2,C5,U7`

Note: PCB width and height has to be added to offsets to avoid negative component origins.

* As above but load settings from config file.

`-i examples/example1.csv -o example1-270-with-rails -cfg examples/example1-google-sheets.properties -r 270 -rx 70 -ry 0 -ox 105 -oy 75 -c`

* Use per-machine and per-project argument files.
```
pnpconvert @machine-settings.pnpconvert @projects/my-project.pnpconvert -s TOP -c
```

`machine-settings.pnpconvert` file content:
```
-f feeders.csv
-t trays.csv
-co components.csv
```

`projects/my-project.pnpconvert` file content:
```
-i projects/my-project.csv
-o projects/my-project
-r 90
-ox 5
-oy 5
```

* Fiducial markers

Fiducial marks can be used for calibration instead of components.

`pnpconvert ... -fm RL,10,87 FR,105,3`

In the example above, `-fm` has two arguments, `RL,10,87` and `FR,105,3` which correspond to Rear-Left and Front-Right markers, the numerical values being X/Y co-ordinates.

Note: Currently only two fiducial markers are supported.

Fiducial markers are useful when you send gerbers files off to a PCB manufacturer who then produce a panel and add markers.  Ask them to send you the panel gerber files
and extract the co-ordinates from the received gerber files.

This is also particularly useful when you have one or more large component footprints in the corner of your PCB and/or cannot visually
select the center of the pattern that the CharmHigh software selects.

* Disable components by refdes

`pnpconvert ... -dr C36,R28`

* Replace components by refdes

`pnpconvert ... -rr "C54,CAP_1206,47uF 10V 1206 X5R 20%" "R25,RES_0402,44k2 0402 1%"`

In the example above, `-rr` has two arguments the order of the values in quotes is RefDes, Name, Value

* Feeder Tester

`pnpcpnvert -ft -cfg machine1.config -o feedertest`

Generates a DPV with all the feeders populated but no placements, this is useful when verifying the contents of all the feeders on the machine.
No placement input file is required.  The names of the feeders are also used to lookup components.


DPV Generation process
======================

4 input files are required to generate a DPV file for your design.

Generate the DipTrace Pick and Place file
Use "File/Export/Pick and Place...".  Ensure the fields as per the example files in the `examples` folder.
The order of the CSV fields does not matter, the column headers are used to find the data.

1. A DipTrace Pick and Place file is read.
2. Any rotation and offset transformations are applied.
3. An updated Pick and Place file, with transformed coordinates and rotation angles is generated.
4. A SVG file is generated.
5. A trays CSV file containing tray definitions is read.
6. A feeders CSV file containing feeder and pick settings is read.
7. A components CSV file which contains component definitions is read.
8. Component and Material selection begins.
9. Materials are assigned.
10. A summary of feeder and tray load-out is presented.
11. Issues are presented (placements with unknown components, inexact components matches, feeders matched by component aliases, unloaded components)
12. The DPV is generated.

The process starts by reading the pick-and-place file cross-referencing each component name and value against
 entries in the components file, then it looks for feeders (reels or trays) that have the component.  When things match up the materials and components tables in the DPV file are generated.

When components don't match a list of unknown components and unloaded feeders is presented which should then be added to the appropriate input files.

Components in the design file that are matched to components using aliases, or to aliases of components already in feeders, are also displayed.  This is so that you can check to see if a substitution is acceptable. E.g. design specifies a capacitor with 10% tolerance, and a similar spec capacitor with 20% tolerance has an alias the same as the 10% one.


SVG file
========

| Color  | Usage  |
| ------ | ------ |
| Red    | Starting Coordinate |
| Yellow | Coordinate after mirroring |
| Blue   | Coordinate after mirroring, rotation |
| Pink   | Coordinate after mirroring, rotation and rotation coordinate offset |
| Green  | Final coordinate after mirroring, rotation, rotation coordinate offset and origin offset |
| Orange | Fiducial Marker |

CSV files
=========

The order of the CSV fields does not matter in input files, the column headers are used to find the data.  This allows you to choose you preferred column order in your CSV editing tools/spreadsheets.
Column headers are case-insensitive, non-alphanumeric characters are converted to underscores before attempting a column header match.

Example Google Sheets spreadsheet with tabs for feeders, trays and components, can be found at the follow public URL:

https://docs.google.com/spreadsheets/d/1-bZiPxQy2budCd0ny81PV6aGKu4q8ckkRBx3FWsMj-M/edit?usp=sharing

Fields requiring additional documentation are as below.

* If you can't work out what a field does after reviewing the fields and your machine documentation please create an issue on the issue tracker asking for more documentation.

Feeders CSV file
================

Columns
-------

| Column | Unit | Notes                                                                                                                                 |
| ------ | -----| ------------------------------------------------------------------------------------------------------------------------------------- |
| FLAGS  | comma separated list | List of flags                                                                                                         |
| ID     | Integer | Required for left/right/front/vibration feeders, optional for "IC tray" feeders.                                                   |
| Enabled | Boolean | Allows the component to be skipped                                                                                                |
| Tray Name | String | Value should correspond with the name of a tray in the Trays CSV file.  A feeder is a 'Tray Feeder' if this field is specified'  |
| Tape Width | Integer, Centimeters | Ignored for Tray feeders.  Still useful for Tray Feeders when using cut-tape in tray feeders.                     |
| Tape Spacing | Integer, Centimeters | Ignored for Tray feeders.  Still useful for Tray Feeders when using cut-tape in tray feeders.                   |
| Place Speed | Integer, Percentage | 0 is invalid.                                                                                                     |
| Place Delay | Milliseconds, Delay after extending head and before retracting head.                                                                    |
| Take Delay | Milliseconds, Delay in milliseconds

Values
------

| Flag | Meaning                                                                                                                |
| !    | Ignore the row.  Use this flag to manage feeders/trays that are not currently in use without having to delete the row  |

Feeder IDs for the CHMT48VB are as below, they are FIXED by the software in the CHMT48VB.

| ID    | Purpose                               |
| ----- | ------------------------------------- |  
| 0-35  | left feeders                          |
| 36-70 | right feeders                         |
| 71-77 | rear left to rear right tray 1-7      |
| 78-83 | front left to front right tray 1-6    |
| 84    | right hand double tray 1/1            |
| 85-90 | vibration feeder, front left.         |
| 91-99 | ic tray                               |

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
* Gradle build tool.  Use included gradle wrapper to install Gradle. via `./gradlew`

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

