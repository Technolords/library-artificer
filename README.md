# Artificer
This library is capable of inspecting a single java library (jar file), by means of byte code analysis,
and creates a XML report. The goal is to provide data which can be fed into other tools. An example of
how such graphical representation could look like:
[Dice skills graphed](http://insights.dice.com/wp-content/themes/dicenews2015/assets/d3/2015/SkillsGraph/index.html)

## Meta data
The meta data reports:
* breakdown of the embedded files. As in, number of .class files, .xml files etc.
* determine the version of .class file (java 7, java 8, etc)
* determine the origin of the referenced classes (self contained, java SE, java EE, other)
* determine implicit support of the library (i.e. presence of OSGI-INF, WEB-INF, META-INF, etc)

There are more features planned, see See [this roadmap](https://github.com/Technolords/library-artificer/projects/1)

## More info
For more information, see the [Wiki](https://github.com/Technolords/techno-tools/wiki)



