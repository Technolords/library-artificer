# Artificer
A tool to inspect a single java library and report its contents. The basic idea is to have the following
information about a java library:
- [x] Breakdown of the files embedded in the jar file. I.e. number of .class files, .xml files, etc
- [x] Against what compiler the class files are generated
- [ ] The implicit dependencies of the class files, as in, which are embedded in thr jar, which are SE and which are neither
- [ ] The classes grouped by package, so this information can be used to see the 'density'
- [ ] A graphical representation of these .classes, by charting them into packages and groups
- [ ] Implicit support of the library, by examining the manifest and support folders (i.e. presence OSGI-ING, WEB-INF, META-INF, etc)

The extended idea is to have:
- [ ] Identification of patterns applied (identifying the interfaces and implementations of those interfaces)
- [ ] Generation of component diagram
- [ ] Generation of sequence diagram
- [ ] Create interactive layer with drill down functionality, so visual scope can be manipulated

## Sequence diagrams
In order to explain the current implementation the following UML sequence diagrams are available:
- [Analyser](/src/main/resources/design/sequence-analyser.png)
- [Artifact Manager](/src/main/resources/design/sequence-artifact-manager.png)