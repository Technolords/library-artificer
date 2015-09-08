package net.technolords.tools.artificer.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Sep-04.
 */
public class Resource {
    /*
2015-09-04 18:21:24,711 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: features.xml, with path: /features.xml
2015-09-04 18:21:24,711 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: StatusProcessor.class, with path: /com/lgi/training/service/recommendation/processor/StatusProcessor.class
2015-09-04 18:21:24,711 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: ErrorProcessor.class, with path: /com/lgi/training/service/recommendation/processor/ErrorProcessor.class
2015-09-04 18:21:24,711 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: RecommendationService.class, with path: /com/lgi/training/service/recommendation/RecommendationService.class
2015-09-04 18:21:24,712 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: RecommendationRouteBuilder.class, with path: /com/lgi/training/service/recommendation/RecommendationRouteBuilder.class
2015-09-04 18:21:24,712 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: blueprint.xml, with path: /OSGI-INF/blueprint/blueprint.xml
2015-09-04 18:21:24,712 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: pom.xml, with path: /META-INF/maven/com.liberty.global/service-recommendation/pom.xml
2015-09-04 18:21:24,712 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: pom.properties, with path: /META-INF/maven/com.liberty.global/service-recommendation/pom.properties
2015-09-04 18:21:24,712 [DEBUG] [main] [net.technolords.tools.artificer.ArtifactResourceVisitor] Found file: MANIFEST.MF, with path: /META-INF/MANIFEST.MF
     */
    private String name;
    private Path path;

    public Resource() {
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
