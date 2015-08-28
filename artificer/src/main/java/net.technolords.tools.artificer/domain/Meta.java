package net.technolords.tools.artificer.domain;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Technolords on 2015-Aug-18.
 */
// <meta status="200" compiled-version="8">
// </meta>
//
// <meta status="500" error-message="No compiled classes found">
// </meta>
public class Meta {
    private String status;
    private String errorMessage;
    private String compiledVersion;

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

    @XmlAttribute(name = "compiled-version")
    public String getCompiledVersion() {
        return compiledVersion;
    }

    public void setCompiledVersion(String compiledVersion) {
        this.compiledVersion = compiledVersion;
    }

}
