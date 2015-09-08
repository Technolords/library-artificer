package net.technolords.tools.artificer;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        this.analysis.getResources().add(resource);
        // Classify resource
        this.classifyResource(resource);
        // return super.visitFile(file, attrs);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
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

    protected void classifyResource(Resource resource) {
        String resourceName = resource.getName();
        if(resourceName.contains(".")) {
            String extension = resourceName.substring(resourceName.lastIndexOf("."));
            // Classify resource under its filename extension
            ResourceGroup resourceGroup = this.analysis.getResourceGroups().get(extension);
            if(resourceGroup == null) {
                resourceGroup = new ResourceGroup();
                resourceGroup.setGroupType(extension);
                this.analysis.getResourceGroups().put(extension, resourceGroup);
            }
            resourceGroup.getResources().add(resource);
//            List<Resource> list = resourceGroups.get(extension);
//            if(list == null) {
//                list = new ArrayList<>();
//                resourceGroups.put(extension, list);
//            }
//            list.add(resource);
        } else {
            // Classify resource as _undefined_classification_
            ResourceGroup resourceGroup = this.analysis.getResourceGroups().get(ArtifactManager.UNDEFINED_CLASSIFICATION);
            if(resourceGroup == null) {
                resourceGroup = new ResourceGroup();
                resourceGroup.setGroupType(ArtifactManager.UNDEFINED_CLASSIFICATION);
                this.analysis.getResourceGroups().put(ArtifactManager.UNDEFINED_CLASSIFICATION, resourceGroup);
            }
            resourceGroup.getResources().add(resource);
//            List<Resource> list = resourceGroups.get(ArtifactManager.UNDEFINED_CLASSIFICATION);
//            if(list == null) {
//                list = new ArrayList<>();
//                resourceGroups.put(ArtifactManager.UNDEFINED_CLASSIFICATION, list);
//            }
//            list.add(resource);
        }
    }

}
