package espam.operations.evaluation;
/**
 * Class, that convert measurement units for power/performance/memory evaluation
 */
public class MeasurementUnitsConverter {

    /** return number of bytes in a megabyte*/
    public static long _MBToBytes(){
        return 1000000;
    }

    /**
     * Get memory size of data types in bytes
     * @param valueDesc data type description
     * @return memory size of data types in bytes
     * @throws Exception if an error occurs
     */
    public static int typeSizeBytes(String valueDesc) {
        if (valueDesc.contains("8"))
            return 1;
        if (valueDesc.contains("16"))
            return 2;
        if (valueDesc.contains("32"))
            return 4;
        if (valueDesc.contains("64"))
            return 8;
        if (valueDesc.contains("128"))
            return 16;
        /** standard shortcuts*/
        /** TODO check*/
        if (valueDesc.equals("bool"))
            return 1;
        if (valueDesc.equals("int"))
            return 4;
        if (valueDesc.equals("float"))
            return 4;
        if (valueDesc.contains("string"))
            return 4;

        System.err.println("mem refinement error: unknown data type " + valueDesc);
        return 0;
    }

    /**
     * Get memory size of data types in bytes
     * @param valueDesc data type description
     * @return memory size of data types in bytes
     * @throws Exception if an error occurs
     */
    public static String javaType(String valueDesc){
        /** standard shortcuts*/
        if(valueDesc.equals("bool"))
            return "boolean";
        if(valueDesc.contains("int"))
            return "int";
        if(valueDesc.equals("float"))
            return "float";
        if(valueDesc.contains("string"))
            return "string";

        System.err.println("mem refinement error: unknown data type " + valueDesc);
        return "null";
    }

    /** get size of one token in bytes*/
    public static Double getTokenSizeInBytes(String dataType){
       return getTokenSize(dataType,false);
    }

    /** get size of one token in megabytes*/
    public static Double getTokenSizeInMegaBytes(String dataType){
        return getTokenSize(dataType,true);
    }

    /** get size of one token*/
    public static Double getTokenSize(String dataType, boolean toMB){
        double tokenSize = (double)MeasurementUnitsConverter.typeSizeBytes(dataType);
        if (toMB)
            tokenSize = tokenSize / (double)MeasurementUnitsConverter._MBToBytes();
        return tokenSize;
    }

    /** get size of one token*/
    public static Long secToMs(){
        return 1000l;
    }
}
