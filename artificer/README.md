# Artificer
A tool to inspect a single java library and report its contents. For more information, see the [Wiki](https://github.com/Technolords/techno-tools/wiki)

Technically, this means to have the following information about a java library:
- DONE: Breakdown of the files embedded in the jar file. I.e. number of .class files, .xml files, etc
- DONE: Determination of which compiler the .class files are generated
- DONE: Find out the references classes, i.e. the implicit dependencies of the .class file (note that a subset of those classesare the imports, however this is bytecode)
- [ ] Classify the dependencies, as in, which are embedded in the jar, which are part of SE and which are neither (3rd party)
- [ ] Determine the implicit support of the library, by examining the manifest and support folders (i.e. presence OSGI-INF, WEB-INF, META-INF, etc)
- [ ] Model the .classes, by grouping them into packages and assign weight factors (preparing for view)
- [ ] Provide a graphical representation of these .classes, using the groups, packages and weight factors (the actual view)

An example of how such graphical representation could look like:
[Dice skills graphed](http://insights.dice.com/wp-content/themes/dicenews2015/assets/d3/2015/SkillsGraph/index.html)

The extended idea is to have:
- [ ] Identification of patterns applied (identifying the interfaces and implementations of those interfaces)
- [ ] Generation of a class diagram
- [ ] Generation of component diagram
- [ ] Generation of sequence diagram
- [ ] Create interactive layer with drill down functionality, so visual scope can be manipulated