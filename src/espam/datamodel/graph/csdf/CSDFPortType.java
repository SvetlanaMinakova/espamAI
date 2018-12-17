package espam.datamodel.graph.csdf;

/**
 * Type of SDF port
 */
public enum CSDFPortType {
    in, out;

     /**
     * Restore sdf graph type from string description
     * By default, type = sdf
     * */
    public static CSDFPortType fromString ( String strType) throws Exception{
        if(strType.equals("in"))
            return CSDFPortType.in;
        if(strType.equals("out"))
            return CSDFPortType.out;

        throw new Exception("port type parse exception: unknown port type " + strType);
    }
}
