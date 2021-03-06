Known issues
============

* diptrace does not quote strings

Workaround: If you have a pattern, component name or component value containing a ',' the program will not work.  Remove commas as appropriate.
Future: allow user to specify diptrace CSV separator

* commas in component names, notes etc.

Workaround: remove them
Future: discard/replace when found on input files and warn user.

* negative rotations

Workaround: always specify positive rotation values

* pick angle and vision system conflict.

When the pick angle is not a right-angle the pick angle will be corrected to the nearest 90 degrees by the vision system
and then the machine angle is applied to the placement.

i.e. If you have a component in a feeder at an angle of 45 degrees the vision system could correct the pick clockwise or anti-clockwise to the nearest right-angle.

If vision is not used then the pick angles that are not 0/90/180/270 can be used.

If you use vision and non-right-angle pick angles your results won't be as expected.

It is unknown if setting the width and height on rectangular components to the correct values helps with the vision correction. Experimentation required.

Would-be-nice
=============

* Display origin in SVG
* Display fiducials in SVG
* Make SVG nicer.
* Set units for SVG display
* Replace specific components by reference designator.

Technology updates
==================
* Grooby 2.5 cli builder - https://picocli.info/groovy-2.5-clibuilder-renewal.html
