package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class MagicNumberParser {

    /**
     * Auxiliary method to extract the magic number associated to the resource. From the 'ClassFile' structure,
     * which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     u4                   magic;
     *     ...
     * }
     *
     *  - magic:
     *      The 'magic' item supplies the magic number identifying the class file format; it has the value 0xCAFEBABE
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractMagicNumber(DataInputStream dataInputStream) throws IOException {
        // Absorb magic number (as it is already known)
        dataInputStream.readInt();
    }
}
