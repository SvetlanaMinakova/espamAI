package espam.datamodel.graph.csdf;

/**
 * SDF graph refined type
 */
public enum SDFGraphType {
    /** static data flow graph*/
    sdf,

    /** cyclo-static data flow graph*/
    csdf;

    /**
     * Restore sdf graph type from string description
     * By default, type = sdf
     * */
    public static SDFGraphType fromString( String strType) {
        if(strType.equals("csdf"))
            return csdf;
        return sdf;
    }

}
