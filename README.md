# PNPConvert

by Dominic Clifton. (C) 2018-2024

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

## Usage

`pnpconvert <args>`

```
PNPConvert (C) 2018-2024 Dominic Clifton
Written by Dominic Clifton
Usage: pnpconvert
      -bblox=<boardBottomLeftOffsetX>
                            origin X - left extent coord (e.g. 0.0 - 5.0 = -5.0)
      -bbloy=<boardBottomLeftOffsetY>
                            origin Y - bottom extent coord (e.g. 0.0 - 5.0 = -5.
                              0)
      -bd=<boardDepth>      Board thickness
      -beox=<boardExportOffsetX>
                            EDA export X offset (e.g. 10.0)
      -beoy=<boardExportOffsetY>
                            EDA export Y offset (e.g. 10.0)
      -bh=<boardHeight>     Board height (not panel height)
      -box=<boardOffsetX>
                            EDA origin X (usually 0.0)
      -boy=<boardOffsetY>
                            EDA origin Y (usually 0.0)
      -bw=<boardWidth>      Board width (not panel width)
  -c                        convert
      -cfg=<config>         configuration file (in "key=value" format)
      -co=<components>      components csv file/url
      -dr=<disableRefdes>   Disable components by refdes (comma separated list)
  -f=<feeders>              feeders csv file/url
      -fm=<fiducialMarkers>...
                            Fiducial marker list (note,x,y[ ...])
      -ft                   Generate DPV containing all feeders
  -i=<input>                input csv file/url
  -j=<job>                  job number
  -m=<mirroring>            mirroring mode (horizontal/vertical/both/none),
                              default is none
  -o=<output>               output prefix
      -ox=<offsetX>         X offset, applied after all other transformations
      -oy=<offsetY>         Y offset, applied after all other transformations
      -oz=<offset>          Z offset, applied to all component heights -
                              increase for thicker PCBs
      -pix=<panelIntervalX> Interval spacing on the X axis
      -piy=<panelIntervalY> Interval spacing on the Y axis
      -pnx=<panelNumberX>   Number of PCBs on the X axis
      -pny=<panelNumberY>   Number of PCBs on the Y axis
      -poy=<panelNumberY>   Number of PCBs on the Y axis
      -prwb=<panelRailBottomWidth>
                            Bottom/Front rail width
      -prwl=<panelRailBottomWidth>
                            Left rail width
      -prwr=<panelRailBottomWidth>
                            Right rail width
      -prwt=<panelRailBottomWidth>
                            Top/Rear rail width
  -r=<rotation>             rotation degrees (positive is clockwise)
      -rr=<replaceRefdes>...
                            Replace components by refdes ("refdes,value,name"[
                              ...])
  -s=<side>                 pcb side (top|bottom|all), default is all
      -st                   Show transforms in SVG
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

## Examples

Example1 has board width of 70 and height of 100, PCB origin is the center.
Offset from center to bottom left is -35,-50. 
When exported "Use PCB origin" was NOT selected, so the bottom left component has a position is closest to 0,0.
U1 is at the PCB origin but in the CSV file its coordinates are relative to the bottom left component.


* Rotate example1 90 degrees

`pnpconvert -i examples/example1.csv -o example1-90 -r 90 -bblox -35 -bbloy -50 -bw 70 -bh 100 -c`


* Rotate example1 90 degrees and add 5mm rails

`pnpconvert -i examples/example1.csv -o example1-90-with-rails -r 90 -bblox -35 -bbloy -50 -bw 70 -bh 100 -oy 5 -ox 5 -c`

* Rotate example1 270 degrees and add 5mm rails

`pnpconvert -i examples/example1.csv -o example1-270-with-rails -r 270 -bblox -35 -bbloy -50 -bw 70 -bh 100 -ox 5 -oy 5 -c`

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

Note: it's also possible to include another argument file from within an argument file, by prefixing the filename with an `@` symbol.

```
pnpconvert @machine-settings.pnpconvert @projects/my-project-top.pnpconvert -c
pnpconvert @machine-settings.pnpconvert @projects/my-project-bottom.pnpconvert -c
```

`machine-settings.pnpconvert` file content:
```
-f feeders.csv
-t trays.csv
-co components.csv
-ox 5
-oy 5
```

`projects/my-project-top.pnpconvert` file content:
```
-i projects/my-project.csv
-o projects/my-project
@project/my-project.panel
-s TOP
```

`projects/my-project-bottom.pnpconvert` file content:
```
-i projects/my-project.csv
-o projects/my-project
@projects/my-project.panel
-s BOTTOM
-r 180
-m HORIZONTAL
```

`projects/my-project.panel` file content:
```
# board
-bblox -35
-bbloy -50
-bw 70
-bh 100

# panel
# intervals includes 5mm gap between boards
-pix 75
-piy 105
-pnx 2
-pny 1
# 5mm rails on all 4 sides
-prwl 5
-prwr 5
-prwt 5
-prwb 5

# fiducials
-fm RL,10,107.5 FR,145,2.5
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

`pnpconvert -ft -cfg machine1.config -o feedertest`

Generates a DPV with all the feeders populated but no placements, this is useful when verifying the contents of all the feeders on the machine.
No placement input file is required.  The names of the feeders are also used to lookup components.


## DPV Generation process

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


## SVG file

| Color  | Usage  |
| ------ | ------ |
| Red    | Starting Coordinate |
| Yellow | Coordinate after mirroring |
| Blue   | Coordinate after mirroring, rotation |
| Pink   | Coordinate after mirroring, rotation and rotation coordinate offset |
| Green  | Final coordinate after mirroring, rotation, rotation coordinate offset and origin offset |
| Orange | Fiducial Marker |
| Magenta | Panel |

## CSV files

The order of the CSV fields does not matter in input files, the column headers are used to find the data.  This allows you to choose you preferred column order in your CSV editing tools/spreadsheets.
Column headers are case-insensitive, non-alphanumeric characters are converted to underscores before attempting a column header match.

Example Google Sheets spreadsheet with tabs for feeders, trays and components, can be found at the follow public URL:

https://docs.google.com/spreadsheets/d/1-bZiPxQy2budCd0ny81PV6aGKu4q8ckkRBx3FWsMj-M/edit?usp=sharing

Fields requiring additional documentation are as below.

* If you can't work out what a field does after reviewing the fields and your machine documentation please create an issue on the issue tracker asking for more documentation.

## Feeders CSV file

### Columns

| Column       | Unit                  | Notes                                                                                                                           |
|--------------|-----------------------|---------------------------------------------------------------------------------------------------------------------------------|
| FLAGS        | comma separated list  | List of flags                                                                                                                   |
| ID           | Integer               | Required for left/right/front/vibration feeders, optional for "IC tray" feeders.                                                |
| Enabled      | Boolean               | Allows the component to be skipped                                                                                              |
| Tray Name    | String                | Value should correspond with the name of a tray in the Trays CSV file.  A feeder is a 'Tray Feeder' if this field is specified' |
| Tape Width   | Integer, Centimeters  | Ignored for Tray feeders.  Still useful for Tray Feeders when using cut-tape in tray feeders.                                   |
| Tape Spacing | Integer, Centimeters  | Ignored for Tray feeders.  Still useful for Tray Feeders when using cut-tape in tray feeders.                                   |
| Place Speed  | Integer, Percentage   | 0 is invalid.                                                                                                                   |
| Place Delay  | Integer, Milliseconds | Delay after extending head and before retracting head.                                                                          |
| Take Delay   | Integer, Milliseconds | Delay in milliseconds.                                                                                                          |

### Values

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

Feeder IDs over 100 are reserved for the machine and must NOT be used otherwise machine configuration is overwritten and head crashes can occur!

## Why
* The existing tools don't support DipTrace.
* The existing tools seem only to support the one EDA package they were designed for, I wanted a codebase that was more flexible to allow support for other packages to be added later.
* The existing tools and are not written in languages that I have tooling set-up for.
* The existing tools have no automated tests, I wan't to know key functionality is correct *before* I feed a file into my machine.
* The existing tools don't really support the CHMT48VB which has an additional feeder bank and some quirks regarding feeder placement.
* The existing tools do not internally use a consistent co-ordinate system.
* I needed something to rotate (not just flip) the co-ordinates and angles in the diptrace files.  Useful when you have board edge components and only 2 rails.
* I wanted to visualize the results of rotation and transform operations.

## Technology Stack
* Java run-time environment - cross-platform.
* Groovy for production and test code.
* Spock for automated unit tests.
* Batik for SVG Generation.
* Gradle build tool.  Use included gradle wrapper to install Gradle. via `./gradlew`

## Running Tests

Running tests:

`gradle test`

## Building

Use the gradle Application plugin's tasks

`gradle installDist`

then run the binaries in `build/install/pnpconvert`

e.g.

`./build/install/pnpconvert/bin/pnpconvert/pnpconvert -v`

Reference: https://docs.gradle.org/current/userguide/application_plugin.html

## Known issues

### Diptrace 3.x does not quote strings in CSV files

Workaround: If you have a pattern, component name or component value containing a ',' the program will not work.  Remove commas as appropriate.
Future: allow user to specify diptrace CSV separator
Note: Diptrace 4.x fixed this.

### Commas in component names, notes etc.

Background: commas cause issues with DPV generation.
Workaround: remove them
Future: discard/replace when found on input files and warn user.

### Negative rotations

Workaround: always specify positive rotation values, i.e. 270, not -90

### Pick angle and vision system conflict.

When the pick angle is not a right-angle the pick angle will be corrected to the nearest 90 degrees by the vision system
and then the machine angle is applied to the placement.

i.e. If you have a component in a feeder at an angle of 45 degrees the vision system could correct the pick clockwise or anti-clockwise to the nearest right-angle.

If vision is not used then the pick angles that are not 0/90/180/270 can be used.

If you use vision and non-right-angle pick angles your results won't be as expected.

It is unknown if setting the width and height on rectangular components to the correct values helps with the vision correction. Experimentation required.
Still, that doesn't help with square components, like ICs, inductors, etc.

## Would-be-nice

* apply rotation/mirroring to panel, fiducials, currently rotation/mirroring only applies to components.
* Set units for SVG display
* Replace specific components by reference designator.

# DPVToGoogleSheets

DPVToGoogleSheets is a work-in-progress tool that will take an updated .dpv file from the PnP machine and update google sheets documents with the changes.

## Usage

`dpvtogooglesheets <args>`

```
DPVtoGoogleSheets (C) 2018-2024 Dominic Clifton
Written by Dominic Clifton
Usage: dpvtogooglesheets
  -c=<credentials>    credentials json file/url
      -cfg=<config>   configuration file (in "key=value" format)
  -i=<input>          input dpv file/url
      -mo=<match-options>...
                      <[FEEDER_ID] [PART_CODE] [MANUFACTURER] [DESCRIPTION]
                        [FLAG_ENABLED] ...>
  -s=<sheet>          sheet id
  -u                  update
  -v                  version
```

Example:
```
dpvtogooglesheets -mo FEEDER_ID FLAG_ENABLED -u -cfg machine1.config -i project.dpv
```

```
dpvtogooglesheets -mo PART_CODE MANUFACTURER FLAG_ENABLED -u -cfg machine1.config -i project.dpv
```

After running the tool, check it's output to see what it changed.

You can also use the built-in version history of the google sheet by opening the sheet
in google sheets, then file/version history, then checking the highlighted cells of the latest version

## Match options

Matching on feeder id works well, however incorrect rows will be updated if you change or re-order the feeder id's
on the PnP machine.

While it's possible to update a sheet using only the match option 'MANUFACTURER' it's not a good idea as it will
potentially match many rows.

Using part code and manufacturer matching options works well, but note if the part code combined with the manufacturer
name is longer than the maximum length allowed in the DPV file then matching will fail. (no partial matches on either).
For this reason it's best to use manufacturer codes, like 'TE', or 'TI', instead of the full name, like 
'T.E. Connectivity' or 'Texas Instruments', respectively.

Matching on description only is bad if part code and manufacturer are used due to the description becoming truncated due
to DPV feeder note length constrants.  Description is matched if is /starts-with/ the same text.

Matching on feeder id and a partial description works well, this is the default.

Matching on feeder id, part code and manufacturer is best, but more strict.

## Troubleshooting

### Bad Request: 'invalid_grant'

* Delete the `tokens/StoredCredential` file and login again.
* Check the credentials json file is correct.

# Future Technology updates

##  Groovy 2.5 cli builder - https://picocli.info/groovy-2.5-clibuilder-renewal.html

# License

GPLv3

