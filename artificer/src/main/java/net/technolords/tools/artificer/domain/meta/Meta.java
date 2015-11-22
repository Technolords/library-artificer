package net.technolords.tools.artificer.domain.meta;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class represents the Meta element, and contains the following attributes:
 *
 * - status         : mandatory element, which represents the overall success, as in
 *                    the value is 200 (OK), or 500 (not OK).
 * - error-message  : optional element, and filled when an error has occurred.
 *
 * Other than the attributes, it contains the following elements:
 *
 * - java-versions  : which contains a list of found java compiler versions, derived
 *                    from the java .class files.
 *
 * Happy flow:
 *
 * <meta status="200">
 *     <java-versions>
 *         <java version="1.7" total-classes="4"></java>
 *     </java-versions>
 * </meta>
 *
 * Unhappy flow:
 *
 * <meta status="500" error-message="No compiled classes found">
 * </meta>
 */
public class Meta {
    private String status;
    private String errorMessage;
    private FoundJavaVersions foundJavaVersions;

    @XmlAttribute(name = "status", required = true)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlAttribute(name = "error-message")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @XmlElement(name = "java-versions")
    public FoundJavaVersions getFoundJavaVersions() {
        return foundJavaVersions;
    }

    public void setFoundJavaVersions(FoundJavaVersions foundJavaVersions) {
        this.foundJavaVersions = foundJavaVersions;
    }

}
