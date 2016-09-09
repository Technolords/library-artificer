package net.technolords.tools.artificer.analyser.dotclass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.dependencies.ReferencedClass;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;

/**
 * Created by Technolords on 2016-May-18.
 */
public class ClassDomainAnalyser {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private Map<String, String> self = new HashMap<>();
    private Map<String, String> standard = new HashMap<>();
    private Map<String, String> enterprise = new HashMap<>();

    public ClassDomainAnalyser(ResourceGroup resourceGroup) {
        // Populate the known class domains
        // TODO: add fragmentation for java version (1.6, 1.7, 1.8)
        this.populateSelfClasses(resourceGroup);
        this.populateStandardClasses();
        this.populateEnterpriseClasses();
    }

    public void analyseReferencedClassForClassDomain(Analysis analysis, Resource resource) {
        if (resource != null) {
            if (!resource.isValidClass()) {
                return;
            }
            ReferencedClass referencedClass = new ReferencedClass();
            referencedClass.setPackageWithClass("java/lang/Object");
            referencedClass.setClassDomain(ReferencedClass.ClassDomain.Standard);
            analysis.getDependencies().add(referencedClass);

            ReferencedClass other = new ReferencedClass();
            other.setPackageWithClass("net/technolords/Cool");
            other.setClassDomain(ReferencedClass.ClassDomain.External);
            analysis.getDependencies().add(other);
        }
    }

    protected void populateSelfClasses(ResourceGroup resourceGroup) {
        if (resourceGroup != null) {
            for(Resource resource : resourceGroup.getResources()) {
                // TODO: find and add
            }
        }
    }

    protected void populateStandardClasses() {
        // TODO: For java 8 source, scan zip file: /usr/lib/jvm/java-8-oracle/src.zip
    }

    protected void populateEnterpriseClasses() {
        // TODO: For EE 7: http://repo1.maven.org/maven2/javax/javaee-api/7.0/
    }
}
