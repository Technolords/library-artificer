package net.technolords.tools.artificer.input;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;

/**
 * Created by Technolords on 2015-Sep-04.
 */
public class ArtifactResourceVisitor implements FileVisitor<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResourceVisitor.class);
    private Analysis analysis;

    public ArtifactResourceVisitor(Analysis analysis) {
        this.analysis = analysis;
    }

    /**
     * Example of visits:
     *
     * Found file: Analyser.class, with path: /net/technolords/tools/artificer/Analyser.class
     * Found file: ArtificerImpl.class, with path: /net/technolords/tools/artificer/ArtificerImpl.class
     * Found file: OutputManager.class, with path: /net/technolords/tools/artificer/output/OutputManager.class
     * Found file: ArtifactManager.class, with path: /net/technolords/tools/artificer/artifact/ArtifactManager.class
     * Found file: ArtifactResourceVisitor.class, with path: /net/technolords/tools/artificer/artifact/ArtifactResourceVisitor.class
     * Found file: ArtificerException.class, with path: /net/technolords/tools/artificer/exception/ArtificerException.class
     * Found file: JavaVersions.class, with path: /net/technolords/tools/artificer/reference/JavaVersions.class
     * Found file: JavaVersion.class, with path: /net/technolords/tools/artificer/reference/JavaVersion.class
     * Found file: JavaVersionManager.class, with path: /net/technolords/tools/artificer/reference/JavaVersionManager.class
     * Found file: ResourceGroup.class, with path: /net/technolords/tools/artificer/domain/ResourceGroup.class
     * Found file: Resource.class, with path: /net/technolords/tools/artificer/domain/Resource.class
     * Found file: Analysis.class, with path: /net/technolords/tools/artificer/domain/Analysis.class
     * Found file: Meta.class, with path: /net/technolords/tools/artificer/domain/Meta.class
     * Found file: java-versions.xml, with path: /reference/java-versions.xml
     * Found file: graph-generation.txt, with path: /design/graph-generation.txt
     * Found file: report.xml, with path: /design/report.xml
     * Found file: pom.properties, with path: /META-INF/maven/net.technolords.tools/artificer/pom.properties
     * Found file: pom.xml, with path: /META-INF/maven/net.technolords.tools/artificer/pom.xml
     * Found file: MANIFEST.MF, with path: /META-INF/MANIFEST.MF
     *
     * @param file
     *  The file associated with the visit.
     * @param attrs
     *  The attributes associated with the file.
     *
     * @return
     *  The continuation of the file visit.
     *
     * @throws IOException
     *  When the file visit fails.
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOGGER.debug("Found file: " + file.getFileName().toString() + ", with path: " + file.toString());

        // Create resource
        Resource resource = new Resource();
        resource.setName(file.getFileName().toString());
        resource.setPath(file);

        // Classify resource
        this.classifyResource(resource);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Overridden method in case the file visit fails. In this case, we log a warning and proceed with visit of
     * other files (i.e. resources in the jar file).
     *
     * @param file
     *  The file associated with the failed visit.
     * @param exception
     *  The exception associated with the failure.
     *
     * @return
     *  The continuation of the file visit.
     *
     * @throws IOException
     *  When the file visit fails.
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
        LOGGER.warn("File (" + file.toString() + ") visit failed, but proceeding: " + exception.getMessage(), exception);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Overridden method in case of a pre visit of a directory.
     *
     * @param dir
     *  The directory associated with the pre-visit.
     * @param attrs
     *  The attributes associated with the directory.
     *
     * @return
     *  The continuation of the file visit.
     *
     * @throws IOException
     *  When the pre-visit fails.
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Overriden method in case of a post visit of a directory.
     *
     * @param dir
     *  The directory associated with the post-visit.
     * @param exception
     *  The exception associated with the post-visit.
     * @return
     *  The continuation of the file visit.
     *
     * @throws IOException
     *  When the post-visit fails.
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Classify the resource based on the extension of the resource. For example, the resources:
     *
     * - sample.class
     * - another.class
     *
     * Will be (both) put under classification '.class'.
     *
     * @param resource
     *  The Resource to be classified.
     */
    protected void classifyResource(Resource resource) {
        String resourceName = resource.getName();
        if(resourceName.contains(".")) {
            String classification = resourceName.substring(resourceName.lastIndexOf("."));
            // Classify resource under its filename extension
            this.addResourceToClassificationGroup(resource, classification);
        } else {
            // Classify resource as _undefined_classification_
            this.addResourceToClassificationGroup(resource, ArtifactManager.CLASSIFICATION_UNDEFINED);
        }
    }

    /**
     * Add the resource to its classification group. If a group does not exist, it will
     * be created.
     *
     * @param resource
     *  The resource to be added.
     * @param classification
     *  The classification group.
     */
    protected void addResourceToClassificationGroup(Resource resource, String classification) {
        ResourceGroup resourceGroup = this.analysis.getResourceGroups().get(classification);
        if(resourceGroup == null) {
            resourceGroup = new ResourceGroup();
            resourceGroup.setGroupType(classification);
            this.analysis.getResourceGroups().put(classification, resourceGroup);
        }
        resourceGroup.getResources().add(resource);
    }

}
