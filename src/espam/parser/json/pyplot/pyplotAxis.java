package espam.parser.json.pyplot;

/**
 * valid pyplot axis names for SDFEvalResult
 */
public enum  pyplotAxis {
    performance,
    memory,
    energy,
    processors;

    /**
     * Get measurement unit for pyplot Axis
     * @return  measurement unit for pyplot Axis
     */
    public static String getMeasurementUnitDesc(pyplotAxis axis){
        switch (axis){
            case performance: return "(seconds)";
            case memory: return "(bytes)";
            case energy: return "(joules)";
            case processors: return "";
            default: return "";
        }

    }

}
