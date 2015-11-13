package net.technolords.tools.artificer.artifact;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.domain.ResourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by Technolords on 2015-Sep-04.
 */
public class ArtifactResourceVisitor implements FileVisitor<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResourceVisitor.class);
    private Analysis analysis;

    public ArtifactResourceVisitor(Analysis analysis) {
        this.analysis = analysis;
    }

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

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
        LOGGER.warn("File (" + file.toString() + ") visit failed, but proceeding: " + e.getMessage(), e);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
