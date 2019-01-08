package espam.datamodel.graph.csdf.datasctructures;

/**
 * Class describes a memory unit inside the SDFNode
 * Used for buffers description and constant parameters,
 * held inside of the CSDF node
 *
 */
public class MemoryUnit implements Cloneable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 /**
     * Clone this Memory Unit
     * @return  a new reference on Memory Unit
     */
    public MemoryUnit clone() {
        try {
            MemoryUnit newObj = (MemoryUnit) super.clone();
            newObj._name = _name;
            newObj._shape = ((Tensor)_shape).clone();
            newObj._typeDesc = _typeDesc;
            newObj._UnitParam = _UnitParam;
            newObj._UnitParamDesc = _UnitParamDesc;
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    public MemoryUnit(MemoryUnit mu){
        _name = mu._name;
        _shape = new Tensor(mu._shape);
        _typeDesc = mu._typeDesc;
        _UnitParam = mu._UnitParam;
        _UnitParamDesc = mu._UnitParamDesc;
    }
    
    /**
     * Create new memory unit with specified name, tensor shape and type description
     * @param name memory unit name
     * @param shape memory unit shape
     * @param typeDesc type description
     */
    public MemoryUnit(String name,Tensor shape, String typeDesc){
        _name = name;
        _shape = shape;
        _typeDesc = typeDesc;
    }

    /**Create constant-value memory unit
     * @param name
     * @param UnitParamDesc
     */
    public MemoryUnit(String name, String UnitParamDesc, String typeDesc){
        _name = name;
        _UnitParamDesc = UnitParamDesc;
        _UnitParam = true;
        _typeDesc = typeDesc;
    }

    ///////////////////////////////////////////////////////////////////
    ////             public getters and setters                   ////

    /**
     * Get type of data, stored in the mem unit
     * @return type of data, stored in the mem unit
     */
    public String getTypeDesc() { return _typeDesc; }

    /**
     * Set type of data, stored in the mem unit
     * @param typeDesc type of data, stored in the mem unit
     */
    public void setTypeDesc(String typeDesc) { this._typeDesc = typeDesc; }


    /** Check, if memory unit is assigned to a port (for buffers)
     * @return true, if memory unit is assigned to a port  and false otherwise
     */
    public boolean isAssigned() { return _assigned; }

    /**
     *Set if memory unit is assigned to a port
     * @param assigned if memory unit is assigned to a port
     */
    public void setAssigned(boolean assigned) { this._assigned = assigned;}

    /**
     * Get memory unit name
     * @return memory unit name
     */
    public String getName() { return _name; }

    /**
     * Set memory unit name
     * @param name memory unit name
     */
    public void setName(String name) { this._name = name; }

    /**
     * Set tensor shape description (for MemoryUnits, storing tensors)
     * @param shape tensor shape description
     */
    public void setShape(Tensor shape) { this._shape = shape; }

    /**
     * Get tensor shape description (for MemoryUnits, storing tensors)
     * @return tensor shape, if MemoryUnit stores tensor and null otherwise
     */
    public Tensor getShape() { return _shape; }

    /**
     * Get dimensionality of memory unit
     * @return dimensionality of tensor, if MemoryUnit stores tensor and 1 otherwise
     */
    public int getDimensionality() {
        if(_shape!=null)
            return _shape.getDimensionality();
        return 1;
    }

    /**
     * Check, if memory unit stores a single (non-tensor) parameter
     * @return true, if memory unit stores a single (non-tensor) parameter
     * and false otherwise
     */
    public boolean isUnitParam() { return _UnitParam; }

    /**
     *Get description of unit parameter (for non-tensor parameters)
     * @return description of unit parameter (for non-tensor parameters)
     */
    public String getUnitParamDesc() { return _UnitParamDesc; }

    /**
     * Set description of unit parameter (for non-tensor parameters)
     * @param UnitParamDesc description of unit parameter (for non-tensor parameters)
     */
    public void setUnitParamDesc(String UnitParamDesc) { this._UnitParamDesc = UnitParamDesc; }

    /**
     * Get default type for memory unit
     * @return default type of data
     */
    public static String getDefaultDataType(){
        return "int";
    }

    ///////////////////////////////////////////////////////////////////
     ////                    private variables                    ////
    /** memory unit name*/
    private String _name;
    
    /** tensor shape description (for MemoryUnits, storing tensors)*/
    private Tensor _shape;
    
    /** is memory unit assigned to a port*/
    private boolean _assigned = false;
    
    /** type of data, stored in memory unit*/
    private String _typeDesc = "int";
    
    /** is memory unit a simple (non-tensor) value*/
    private boolean _UnitParam = false;
    
    /** unit value description - for non-tensor memory units*/
    private String _UnitParamDesc;
}
