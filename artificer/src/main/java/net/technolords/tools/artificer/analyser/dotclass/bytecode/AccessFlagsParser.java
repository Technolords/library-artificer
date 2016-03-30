package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class AccessFlagsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessFlagsParser.class);

    /**
     * Access flags can have a different location, namely:
     * - ClassFile
     * - field_info
     * - method_info
     *
     * And depending on the location, the value or info has different semantics.
     */
    public static final String LOCATION_CLASS_FILE = "CLASS";
    public static final String LOCATION_NESTED_CLASS_FILE = "INNER_CLASS";
    public static final String LOCATION_FIELD_INFO = "FIELD";
    public static final String LOCATION_METHOD_INFO = "METHOD";

    private static final int FLAG_PUBLIC       = 0x0001;        // Class, Inner class, Field, Method
    private static final int FLAG_PRIVATE      = 0x0002;        //        Inner class, Field, Method
    private static final int FLAG_PROTECTED    = 0x0004;        //        Inner class, Field, Method
    private static final int FLAG_STATIC       = 0x0008;        //        Inner class, Field, Method
    private static final int FLAG_FINAL        = 0x0010;        // Class, Inner class, Field, Method
    private static final int FLAG_SUPER        = 0x0020;        // Class
    private static final int FLAG_SYNCHRONIZED = 0x0020;        //                            Method
    private static final int FLAG_VOLATILE     = 0x0040;        //                     Field
    private static final int FLAG_BRIDGE       = 0x0040;        //                            Method
    private static final int FLAG_TRANSIENT    = 0x0080;        //                     Field
    private static final int FLAG_VAR_ARGS     = 0x0080;        //                            Method
    private static final int FLAG_NATIVE       = 0x0100;        //                            Method
    private static final int FLAG_INTERFACE    = 0x0200;        // Class, Inner class
    private static final int FLAG_ABSTRACT     = 0x0400;        // Class, Inner class,        Method
    private static final int FLAG_STRICT       = 0x0800;        //                            Method
    private static final int FLAG_SYNTHETIC    = 0x1000;        // Class, Inner class, Field, Method
    private static final int FLAG_ANNOTATION   = 0x2000;        // Class, Inner class
    private static final int FLAG_ENUM         = 0x4000;        // Class, Inner class, Field

    private static final Map<Integer, String> masksForClass = Collections.unmodifiableMap(
        Stream.of(
            AccessFlagsParser.entry(FLAG_ENUM, "enum"),
            AccessFlagsParser.entry(FLAG_ANNOTATION, "annotation"),
            AccessFlagsParser.entry(FLAG_SYNTHETIC, "synthetic"),
            AccessFlagsParser.entry(FLAG_ABSTRACT, "abstract"),
            AccessFlagsParser.entry(FLAG_INTERFACE, "interface"),
            AccessFlagsParser.entry(FLAG_SUPER, "super"),
            AccessFlagsParser.entry(FLAG_FINAL, "final"),
            AccessFlagsParser.entry(FLAG_PUBLIC, "public")
        ).collect(AccessFlagsParser.entriesToMap())
    );

    private static final Map<Integer, String> masksForInnerClass = Collections.unmodifiableMap(
        Stream.of(
            AccessFlagsParser.entry(FLAG_ENUM, "enum"),
            AccessFlagsParser.entry(FLAG_ANNOTATION, "annotation"),
            AccessFlagsParser.entry(FLAG_SYNTHETIC, "synthetic"),
            AccessFlagsParser.entry(FLAG_ABSTRACT, "abstract"),
            AccessFlagsParser.entry(FLAG_INTERFACE, "interface"),
            AccessFlagsParser.entry(FLAG_FINAL, "final"),
            AccessFlagsParser.entry(FLAG_STATIC, "static"),
            AccessFlagsParser.entry(FLAG_PROTECTED, "protected"),
            AccessFlagsParser.entry(FLAG_PRIVATE, "private"),
            AccessFlagsParser.entry(FLAG_PUBLIC, "public")
        ).collect(AccessFlagsParser.entriesToMap())
    );

    private static final Map<Integer, String> masksForField = Collections.unmodifiableMap(
        Stream.of(
            AccessFlagsParser.entry(FLAG_ENUM, "enum"),
            AccessFlagsParser.entry(FLAG_SYNTHETIC, "synthetic"),
            AccessFlagsParser.entry(FLAG_TRANSIENT, "transient"),
            AccessFlagsParser.entry(FLAG_VOLATILE, "volatile"),
            AccessFlagsParser.entry(FLAG_FINAL, "final"),
            AccessFlagsParser.entry(FLAG_STATIC, "static"),
            AccessFlagsParser.entry(FLAG_PROTECTED, "protected"),
            AccessFlagsParser.entry(FLAG_PRIVATE, "private"),
            AccessFlagsParser.entry(FLAG_PUBLIC, "public")
        ).collect(AccessFlagsParser.entriesToMap())
    );

    private static final Map<Integer, String> masksForMethod = Collections.unmodifiableMap(
        Stream.of(
            AccessFlagsParser.entry(FLAG_SYNTHETIC, "synthetic"),
            AccessFlagsParser.entry(FLAG_STRICT, "strict"),
            AccessFlagsParser.entry(FLAG_ABSTRACT, "abstract"),
            AccessFlagsParser.entry(FLAG_NATIVE, "native"),
            AccessFlagsParser.entry(FLAG_VAR_ARGS, "varargs"),
            AccessFlagsParser.entry(FLAG_BRIDGE, "bridge"),
            AccessFlagsParser.entry(FLAG_SYNCHRONIZED, "synchronized"),
            AccessFlagsParser.entry(FLAG_FINAL, "final"),
            AccessFlagsParser.entry(FLAG_STATIC, "static"),
            AccessFlagsParser.entry(FLAG_PROTECTED, "protected"),
            AccessFlagsParser.entry(FLAG_PRIVATE, "private"),
            AccessFlagsParser.entry(FLAG_PUBLIC, "public")
        ).collect(AccessFlagsParser.entriesToMap())
    );

    public static void extractAccessFlags(DataInputStream dataInputStream, String location) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AccessFlags (for: ").append(location).append("): ");

        int accessFlags = dataInputStream.readUnsignedShort();
        buffer.append(String.join(", ", extractAccessFlags(accessFlags, location)));
        LOGGER.debug(buffer.toString());
    }

    protected static List<String> extractAccessFlags(int accessFlags, String location) {
        List<String> flags = new ArrayList<>();
        if(accessFlags == 0) {
            flags.add("None");
        } else {
            switch (location) {
                case LOCATION_CLASS_FILE:
                    checkBitMasks(masksForClass, accessFlags, flags);
                    break;
                case LOCATION_NESTED_CLASS_FILE:
                    checkBitMasks(masksForInnerClass, accessFlags, flags);
                    break;
                case LOCATION_FIELD_INFO:
                    checkBitMasks(masksForField, accessFlags, flags);
                    break;
                case LOCATION_METHOD_INFO:
                    checkBitMasks(masksForMethod, accessFlags, flags);
                    break;
                default:
                    LOGGER.debug("Unable to extract access flags, unsupported location: " + location);
            }
        }
        return flags;
    }

    protected static void checkBitMasks(Map<Integer, String> bitMasks, int accessFlags, List<String> flags) {
        for(Integer mask : bitMasks.keySet()) {
            if((accessFlags & mask) != 0) {
                LOGGER.trace("Checking for mask: " + mask + ", adding flag: " + bitMasks.get(mask));
                flags.add(bitMasks.get(mask));
            } else {
                LOGGER.trace("Checking for mask: " + mask + ", no match...");
            }
        }
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap((e) -> (e).getKey(), (e) -> (e).getValue());
    }

}
