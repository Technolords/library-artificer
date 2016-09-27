# Artificer
This library is capable of inspecting a single java library (jar file). It does this by means of byte code analysis per class file found.
Per class meta data (result of the analysis) is kept and in the end a full report is created in XML format for the entire library.

The goal is to provide meta data which can be fed into other tools, which, for example is to visualize the meta data. An example
of such visualization is a layout of the classes. Such a graph, could look like:
[Dice skills graphed](http://insights.dice.com/wp-content/themes/dicenews2015/assets/d3/2015/SkillsGraph/index.html)

## Meta data
The meta data reports:
* breakdown of the embedded files. As in, number of .class files, .xml files etc.
* info about the compiled version of the .class file (i.e. java 7, java 8, etc)
* info about the dependencies by recognizing the origin of the referenced classes (as in: self packaged, java SE, java EE, third party)
* info about the utilization of the library (i.e. presence of OSGI-INF, WEB-INF, META-INF, etc)

There are more features planned, see [this roadmap](https://github.com/Technolords/library-artificer/projects/1)

## More info
For more information, see the [wiki here](https://github.com/Technolords/techno-tools/wiki)



