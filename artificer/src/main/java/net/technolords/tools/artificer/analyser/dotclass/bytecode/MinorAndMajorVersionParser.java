package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class MinorAndMajorVersionParser {

    /**
     * Auxiliary method to extract the minor and major version associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   minor_version;
     *     u2                   major_version;
     *     ...
     * }
     *
     *  - minor_version, major_version:
     *      The values of the 'minor_version' and 'major_version' items are the minor and major version numbers of
     *      this class file. Together, a major and a minor version number determine the version of the class file
     *      format. If a class file has major version number M and minor version number m, we denote the version
     *      of its class file format as M.m. Thus, class file format versions may be ordered lexicographically,
     *      for example, 1.5 < 2.0 < 2.1.
     *
     *      A Java Virtual Machine implementation can support a class file format of version v if and only if v lies
     *      in some contiguous range Mi.0 ≤ v ≤ Mj.m. The release level of the Java SE platform to which a Java
     *      Virtual Machine implementation conforms is responsible for determining the range.
     *
     *      Oracle's Java Virtual Machine implementation in JDK release 1.0.2 supports class file format versions 45.0
     *      through 45.3 inclusive. JDK releases 1.1.* support class file format versions in the range 45.0 through
     *      45.65535 inclusive. For k ≥ 2, JDK release 1.k supports class file format versions in the range 45.0
     *      through 44+k.0 inclusive.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractMinorAndMajorVersion(DataInputStream dataInputStream) throws IOException {
        // Absorb minor and major version (as it is already known)
        dataInputStream.readInt();
    }
}
