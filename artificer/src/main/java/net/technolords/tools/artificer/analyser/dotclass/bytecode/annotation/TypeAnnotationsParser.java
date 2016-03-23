package net.technolords.tools.artificer.analyser.dotclass.bytecode.annotation;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class TypeAnnotationsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeAnnotationsParser.class);

    /**
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param annotationsCount
     *  The number of annotations.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractTypeAnnotations(DataInputStream dataInputStream, int annotationsCount, Resource resource) throws IOException {
        for(int index = 0; index < annotationsCount; index++) {
            extractTypeAnnotation(dataInputStream, index, resource);
        }
    }

    /**
     *
     * type_annotation {
     *      u1                          target_type;
     *
     *      union {
     *          {
     *              u1                  type_parameter_index;
     *          } type_parameter_target;
     *
     *          {
     *              u2                  supertype_index;
     *          } supertype_target;
     *
     *          {
     *              u1                  type_parameter_index;
     *              u1                  bound_index;
     *          } type_parameter_bound_target;
     *
     *          {
     *          } empty_target;
     *
     *          {
     *              u1                  formal_parameter_index;
     *          } formal_parameter_target;
     *
     *          {
     *              u2                  throws_type_index;
     *          } throws_target;
     *
     *          {
     *              u2                  table_length;
     *              {
     *                  u2              start_pc;
     *                  u2              length;
     *                  u2              index;
     *              } table[table_length];
     *          } localvar_target;
     *
     *          {
     *              u2                  exception_table_index;
     *          } catch_target;
     *
     *          {
     *              u2                  offset;
     *          } offset_target;
     *
     *          {
     *              u2                  offset;
     *              u1                  type_argument_index;
     *          } type_argument_target;
     *      } target_info;
     *
     *      type_path                   target_path;
     *
     *      u2                          type_index;
     *      u2                          num_element_value_pairs;
     *      {
     *          u2                      element_name_index;
     *          element_value           value;
     *      } element_value_pairs[num_element_value_pairs];
     *  }
     *
     *  The first three items - target_type, target_info, and target_path - specify the precise location of the annotated type.
     *  The last three items - type_index, num_element_value_pairs, and element_value_pairs[] - specify the annotation's
     *  own type and element-value pairs.
     *
     *  - target_type:
     *      The value of the 'target_type' item denotes the kind of target on which the annotation appears.
     *      The various kinds of target correspond to the type contexts of the Java programming language where types are
     *      used in declarations and expressions. The legal values of 'target_type' are specified below. Each value is
     *      a one-byte tag indicating which item of the 'target_info' union follows the 'target_type' item to give more
     *      information about the target.
     *
     *      Value       target_info item            Kind of target
     *      0x00        type_parameter_target       type parameter declaration of generic class or interface
     *      0x01        type_parameter_target       type parameter declaration of generic method or constructor
     *      0x10        supertype_target            type in extends or implements clause of class declaration (including the
     *                                              direct superclass or direct superinterface of an anonymous class declaration),
     *                                              or in extends clause of interface declaration
     *      0x11        type_parameter_bound_target type in bound of type parameter declaration of generic class or interface
     *      0x12        type_parameter_bound_target type in bound of type parameter declaration of generic method or constructor
     *      0x13        empty_target                type in field declaration
     *      0x14        empty_target                return type of method, or type of newly constructed object
     *      0x15        empty_target                receiver type of method or constructor
     *      0x16        formal_parameter_target     type in formal parameter declaration of method, constructor, or lambda expression
     *      0x17        throws_target               type in throws clause of method or constructor
     *      0x40        localvar_target             type in local variable declaration
     *      0x41        localvar_target             type in resource variable declaration
     *      0x42        catch_target                type in exception parameter declaration
     *      0x43        offset_target               type in instanceof expression
     *      0x44        offset_target               type in new expression
     *      0x45        offset_target               type in method reference expression using ::new
     *      0x46        offset_target               type in method reference expression using ::Identifier
     *      0x47        type_argument_target        type in cast expression
     *      0x48        type_argument_target        type argument for generic constructor in new expression or explicit
     *                                              constructor invocation statement
     *      0x49        type_argument_target        type argument for generic method in method invocation expression
     *      0x4A        type_argument_target        type argument for generic constructor in method reference expression using ::new
     *      0x4B        type_argument_target        type argument for generic method in method reference expression using ::Identifier
     *
     * - target_info:
     *      The value of the 'target_info' item denotes precisely which type in a declaration or expression is annotated.
     * - target_path:
     *      The value of the 'target_path' item denotes precisely which part of the type indicated by 'target_info' is annotated.
     * - type_index, num_element_value_pairs, element_value_pairs[]:
     *      The meaning of these items in the type_annotation structure is the same as their meaning in the annotation structure
     *
     * type_path {
     *      u1                          path_length;
     *      {
     *          u1                      type_path_kind;
     *          u1                      type_argument_index;
     *      } path[path_length];
     *  }
     *
     * @param dataInputStream
     * @param index
     * @param resource
     * @throws IOException
     */
    protected static void extractTypeAnnotation(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        // Read target_type (tag)
        int tag = dataInputStream.readUnsignedByte();
        StringBuilder buffer = new StringBuilder();
        buffer.append("Type annotation (index: ").append(index).append("), with targetType: ").append(tag);

        // Read target_info
        switch (tag) {
            case 0x00: // target_info: type_parameter_target
            case 0x01: // target_info: type_parameter_target
                // The 'type_parameter_target' item indicates that an annotation appears on the declaration of the i'th
                // type parameter of a generic class, generic interface, generic method, or generic constructor.
                // The value of the 'type_parameter_index' item specifies which type parameter declaration is annotated.
                // A 'type_parameter_index' value of 0 specifies the first type parameter declaration.

                int typeParameterIndex1 = dataInputStream.readUnsignedByte();
                buffer.append(", with targetInfo: type_parameter_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x10: // target_info: supertype_target
                // The 'supertype_target' item indicates that an annotation appears on a type in the extends or implements
                // clause of a class or interface declaration.

                int superTypeIndex = dataInputStream.readUnsignedShort();
                buffer.append(", with targetInfo: supertype_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case and see if descriptor needs to be added...
                break;

            case 0x11: // target_info: type_parameter_bound_target
            case 0x12: // target_info: type_parameter_bound_target
                // The 'type_parameter_bound_target' item indicates that an annotation appears on the i'th bound of
                // the j'th type parameter declaration of a generic class, interface, method, or constructor.
                // The value of the of 'type_parameter_index' item specifies which type parameter declaration has an
                // annotated bound. A 'type_parameter_index' value of 0 specifies the first type parameter declaration.
                // The value of the 'bound_index' item specifies which bound of the type parameter declaration
                // indicated by 'type_parameter_index' is annotated. A bound_index value of 0 specifies the first
                // bound of a type parameter declaration
                // The 'type_parameter_bound_target' item records that a bound is annotated, but does not record the
                // type which constitutes the bound. The type may be found by inspecting the class signature or
                // method signature stored in the appropriate Signature attribute.

                int typeParameterIndex2 = dataInputStream.readUnsignedByte();
                int boundIndex = dataInputStream.readUnsignedByte();
                buffer.append(", with targetInfo: type_parameter_bound_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x13: // target_info: empty_target
            case 0x14: // target_info: empty_target
            case 0x15: // target_info: empty_target
                // The 'empty_target' item indicates that an annotation appears on either the type in a field
                // declaration, the return type of a method, the type of a newly constructed object, or the
                // receiver type of a method or constructor.

                buffer.append(", with targetInfo: empty_target");
                LOGGER.debug(buffer.toString());
                break;

            case 0x16: // target_info: formal_parameter_target
                // The 'formal_parameter_target' item indicates that an annotation appears on the type in a formal
                // parameter declaration of a method, constructor, or lambda expression.
                // The value of the 'formal_parameter_index' item specifies which formal parameter declaration has an
                // annotated type. A 'formal_parameter_index' value of 0 specifies the first formal parameter declaration.
                // The 'formal_parameter_target' item records that a formal parameter's type is annotated, but does
                // not record the type itself. The type may be found by inspecting the method descriptor of the
                // 'method_info' structure enclosing the RuntimeVisibleTypeAnnotations attribute. A 'formal_parameter_index'
                // value of 0 indicates the first parameter descriptor in the method descriptor.

                int formalParameterIndex = dataInputStream.readUnsignedByte();
                buffer.append(", with targetInfo: formal_parameter_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x17: // target_info: throws_target
                // The 'throws_target' item indicates that an annotation appears on the i'th type in the throws clause
                // of a method or constructor declaration.
                // The value of the 'throws_type_index' item is an index into the 'exception_index_table' array of the
                // Exceptions attribute of the 'method_info' structure enclosing the RuntimeVisibleTypeAnnotations attribute.

                int throwsTypeIndex = dataInputStream.readUnsignedShort();
                buffer.append(", with targetInfo: throws_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x40: // target_info: localvar_target
            case 0x41: // target_info: localvar_target
                // The 'localvar_target' item indicates that an annotation appears on the type in a local variable
                // declaration, including a variable declared as a resource in a try-with-resources statement.
                // localvar_target {
                //      u2          table_length;
                //      {   u2      start_pc;
                //          u2      length;
                //          u2      index;
                //      } table[table_length];
                // }
                // The value of the 'table_length' item gives the number of entries in the table array. Each entry
                // indicates a range of code array offsets within which a local variable has a value. It also indicates
                // the index into the local variable array of the current frame at which that local variable can be
                // found. Each entry contains the following three items:
                // - start_pc, length:
                //      The given local variable has a value at indices into the code array in the interval
                //      [start_pc, start_pc + length), that is, between start_pc inclusive and start_pc + length exclusive.
                // - index:
                //      The given local variable must be at index in the local variable array of the current frame.
                //      If the local variable at index is of type double or long, it occupies both index and index + 1.
                // A table is needed to fully specify the local variable whose type is annotated, because a single local
                // variable may be represented with different local variable indices over multiple live ranges. The
                // start_pc, length, and index items in each table entry specify the same information as a LocalVariableTable attribute.
                // The localvar_target item records that a local variable's type is annotated, but does not record the
                // type itself. The type may be found by inspecting the appropriate LocalVariableTable attribute.

                int tableLength = dataInputStream.readUnsignedShort();
                int localVarStartPc, localVarLength, localVarIndex;
                for(int x = 0 ; x < tableLength ; x++) {
                    localVarStartPc = dataInputStream.readUnsignedShort();
                    localVarLength = dataInputStream.readUnsignedShort();
                    localVarIndex = dataInputStream.readUnsignedShort();
                }
                buffer.append(", with targetInfo: localvar_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x42: // target_info: catch_target
                // The 'catch_target' item indicates that an annotation appears on the i'th type in an exception parameter declaration.
                // The value of the 'exception_table_index' item is an index into the 'exception_table' array of the
                // Code attribute enclosing the RuntimeVisibleTypeAnnotations attribute.
                // The possibility of more than one type in an exception parameter declaration arises from the multi-catch
                // clause of the try statement, where the type of the exception parameter is a union of types. A compiler
                // usually creates one 'exception_table' entry for each type in the union, which allows the 'catch_target'
                // item to distinguish them. This preserves the correspondence between a type and its annotations.

                int exceptionTableIndex = dataInputStream.readUnsignedShort();
                buffer.append(", with targetInfo: catch_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x43: // target_info: offset_target
            case 0x44: // target_info: offset_target
            case 0x45: // target_info: offset_target
            case 0x46: // target_info: offset_target
                // The 'offset_target' item indicates that an annotation appears on either the type in an instanceof
                // expression or a new expression, or the type before the :: in a method reference expression.
                // The value of the offset item specifies the code array offset of either the instanceof bytecode
                // instruction corresponding to the instanceof expression, the new bytecode instruction corresponding
                // to the new expression, or the bytecode instruction corresponding to the method reference expression.

                int offsetTarget = dataInputStream.readUnsignedShort();
                buffer.append(", with targetInfo: offset_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            case 0x47: // target_info: type_argument_target
            case 0x48: // target_info: type_argument_target
            case 0x49: // target_info: type_argument_target
            case 0x4A: // target_info: type_argument_target
            case 0x4B: // target_info: type_argument_target
                // The 'type_argument_target' item indicates that an annotation appears either on the i'th type in a
                // cast expression, or on the i'th type argument in the explicit type argument list for any of the
                // following: a new expression, an explicit constructor invocation statement, a method invocation
                // expression, or a method reference expression.
                // The value of the offset item specifies the code array offset of either the bytecode instruction
                // corresponding to the cast expression, the new bytecode instruction corresponding to the new
                // expression, the bytecode instruction corresponding to the explicit constructor invocation statement,
                // the bytecode instruction corresponding to the method invocation expression, or the bytecode
                // instruction corresponding to the method reference expression.

                int offsetArgumentTarget = dataInputStream.readUnsignedShort();
                int typeArgumentIndex = dataInputStream.readUnsignedByte();
                buffer.append(", with targetInfo: type_argument_target");
                LOGGER.debug(buffer.toString());
                // TODO: create test case
                break;

            default:
                buffer.append(", with targetInfo: unsupported tag!!");
                LOGGER.debug(buffer.toString());
        }

        /**
         *      type_path                   target_path;
         *
         *      type_path {
         *          u1                      path_length;
         *          {   u1                  type_path_kind;
         *              u1                  type_argument_index;
         *          } path[path_length];
         *      }
         *
         *      u2                          type_index;
         *      u2                          num_element_value_pairs;
         *      {
         *          u2                      element_name_index;
         *          element_value           value;
         *      } element_value_pairs[num_element_value_pairs];
         */

        StringBuilder pathBuffer = new StringBuilder();

        // Read target_path
        int pathLength = dataInputStream.readUnsignedByte();
        int typePathKind, typeArgumentIndex;
        pathBuffer.append("Type annotation (index: ").append(index).append("), with path_length: ").append(pathLength);
        for(int i = 0; i < pathLength; i++) {
            typePathKind = dataInputStream.readUnsignedByte();
            typeArgumentIndex = dataInputStream.readUnsignedByte();
        }

        // Read type_index
        int typeIndex = dataInputStream.readUnsignedShort();
        String descriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), typeIndex);
        pathBuffer.append(", with (type_index: ").append(typeIndex).append("): ").append(descriptor);
        LOGGER.debug(pathBuffer.toString());

        // Add signature (when applicable) to the referenced classes
        SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), descriptor);

        // Read num_element_value_pairs
        int numberElementValuePairs = dataInputStream.readUnsignedShort();
        AnnotationsParser.extractElementValuePairs(dataInputStream, numberElementValuePairs, resource);
    }
}
