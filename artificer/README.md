# Artificer
A tool to inspect a single java library and report its contents. The basic idea is to have the following
information about a java library:
- [x] Breakdown of the files embedded in the jar file. I.e. number of .class files, .xml files, etc
- [x] Determination of which compiler the .class files are generated
- [ ] Find out the implicit dependencies of .the class files, as in, which are embedded in the jar, which are part of SE and which are neither
- [ ] A graphical representation of these .classes, by charting them into packages and groups
- [ ] Implicit support of the library, by examining the manifest and support folders (i.e. presence OSGI-ING, WEB-INF, META-INF, etc)

An example of how such graphical representation could look like:
[Dice skills graphed](http://insights.dice.com/wp-content/themes/dicenews2015/assets/d3/2015/SkillsGraph/index.html)

The extended idea is to have:
- [ ] Identification of patterns applied (identifying the interfaces and implementations of those interfaces)
- [ ] Generation of component diagram
- [ ] Generation of sequence diagram
- [ ] Create interactive layer with drill down functionality, so visual scope can be manipulated

## Sequence diagrams
In order to explain the current implementation the following UML sequence diagrams are available:
- [Analyser](/src/main/resources/design/sequence-analyser.png)
- [Artifact Manager](/src/main/resources/design/sequence-artifact-manager.png)