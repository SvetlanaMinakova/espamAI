package espam.datamodel.graph.csdf;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.Node;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CSDFGraphVisitor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
 * A port of an CSDFNode (Actor)
 */
public class CSDFPort extends NPort {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

      /**
     * Constructor to create an CSDFPort with a name and initial rate
     * @param name sdf port name
     * @param id sdf port id
     * @param type sdf port type
     */
    public CSDFPort (String name, int id, CSDFPortType type) {
        super(name);
        setId(id);
        _rates = new Vector<>();
        setType(type);
    }

    /** Accept a Visitor
     *
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CSDFGraphVisitor x) { x.visitComponent(this); }

    /**
     *  Clone this CSDFPort
     *
     * @return  a new instance of the CSDFPort.
     */
    public Object clone() {
        CSDFPort newObj = (CSDFPort) super.clone();
        newObj.setId(_id);
        newObj.setRates((Vector<IndexPair>)_rates.clone());
        newObj.setNodeId(_nodeId);
        newObj._overlapHandler = _overlapHandler;
        newObj._group = _group;
        newObj._assignedMemory = ((MemoryUnit)_assignedMemory).clone();
        newObj._overlapPair = _overlapPair;
        return( newObj );
    }

    /**
     *  Return a description of the CSDFPort.
     * @return  a description of the CSDFPort.
     */
    public String toString() {
        String port = getName() + ", rate: "+ getStrRates() + " ,type: " + _type.toString();
        return port;
    }

    /**
     * Get string description of the port rates
     * @return string description of the port rates
     */
    public String getStrRates(){
        if(_rates==null)
            return "";

        String strRates = "[";
        for (IndexPair rate: _rates){
            strRates += rate.getSecond() + " * " + rate.getFirst() + " ,";
        }

        /** remove las comma */
        if(_rates.size() > 0) {
            strRates = strRates.substring(0, strRates.length() - 1);
        }

        strRates += "]";
        return strRates;
    }

    /**
     * Compares CSDFPort with another object
     * @param obj Object to compare this CSDFPort with
     * @return true if CSDFPort is equal to the object and false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        CSDFPort port = (CSDFPort)obj;

        return _id == port._id
                && this.getType().equals(port.getType())
                && _nodeId == port._nodeId
                && _isRatesEqual(((CSDFPort) obj).getRates())
                && _overlapHandler == (((CSDFPort) obj).isOverlapHandler())
                && getName().equals(port.getName());
    }

    /**
     * Returns SDFG Edge
     *
     * @return SDFG Edge
     */
    public CSDFEdge getEdge() { return (CSDFEdge)super.getEdge(); }

    /**
     * Get port unique identifier
     * @return port unique identifier
     */
    public int getId() { return _id; }

    /**
     * Set port unique identifier
     * @param id port unique identifier
     */
    public void setId(int id) { this._id = id; }

    /**
     * Get port's owner(node) Id
     * @return  port's owner(node) Id
     */
    public int getNodeId() { return _nodeId; }

    /**
     * Set port's owner(node) Id
     * @param nodeId port's owner(node) Id
     */
    public void setNodeId(int nodeId) {
        this._nodeId = nodeId;
    }

    /**
     * set CSDFNode
     * @param node
     */
    public void setNode(CSDFNode node) {
        super.setNode(node);
        setNodeId(node.getId());
    }

    /**
     * Get sdf port type
     * @return sdf port type
     */
    public CSDFPortType getType() { return _type; }

    /**
     * Set sdf port type
     * @param type sdf port type
     */
    public void setType(CSDFPortType type){ _type = type; }

    /**
     * Add new rate to the rates list
     * @param rate rate to be added to the rates list
     */
    public void addRate(int rate, int repetitions){
        IndexPair rateDesc = new IndexPair(rate,repetitions);
        _rates.add(rateDesc);
    }

    /**
     * Remover rate from the rates list
     * @param rate rate to be removed
     */
    public void removeRate(int rate){
        _rates.remove(rate);
    }


    /**
     * Remover rate from the rates list by id
     * @param id pf the rate to be removed
     */
    public void removeRateById(int id){
        IndexPair rateToRemove = _rates.elementAt(id);
        _rates.remove(rateToRemove);

    }

    /**
     * Get port rates
     * @return port rates
     */
    public Vector<IndexPair> getRates() { return _rates; }

    /**
     * Set CSDF rates
     * @param rates rates
     */
    public void setRates(Vector<IndexPair> rates) {
        this._rates = rates;
    }

    /**
     * Checks, if the port is a special self-loop port,
     * helping to process input data overlapping
     * @return true, if the port is a special overlap-handler port,
     * and false otherwise
     */
    public boolean isOverlapHandler() {
        return _overlapHandler;
    }

    /**
     * Make/unmake a port to be a special self-loop port,
     * helping to process input data overlapping
     * @param overlapHandler flag, shows if the port is overlap handler
     */
    public void setOverlapHandler(boolean overlapHandler) {
        this._overlapHandler = overlapHandler;
    }

    /**
     * Get port group
     * @return port group
     */
    public Integer getGroup() {
        return _group;
    }

    /**
     * Set port group
     * @param group port group
     */
    public void setGroup(Integer group) {
        this._group = group;
    }

    /**
     * Get number of tokens to start reading/writing with
     * @return number of tokens to start reading/writing with
     */
    public Vector<IndexPair> getStartTokens() {
        return _startTokens;
    }

    /**
     * Set number of tokens to start reading/writing with
     * @param startTokens number of tokens to start reading/writing with
     */
    public void setStartTokens(Vector<IndexPair> startTokens) {
        this._startTokens = startTokens;
    }

    /**
     * Get assigned memory dimensionality. By default, assigned memory dimensionality = 1
     * @return assigned memory dimensionality. By default, assigned memory dimensionality = 1
     */
    public int getMemoryDim(){
        if(_assignedMemory==null)
            return 1;
        return _assignedMemory.getDimensionality();
    }

    /**
     * Set assigned memory description
     * @param assignedMemory assigned memory description
     */
    public void setAssignedMemory(MemoryUnit assignedMemory) {
        this._assignedMemory = assignedMemory;
    }

    /**
     * Get assigned memory description
     * @return assigned memory description
     */
    public MemoryUnit getAssignedMemory() {
        return _assignedMemory;
    }

    /**
     * Get name of the overlap port pair
     * @return name of the overlap port pair
     */
    public String getOverlapPair() {
        return _overlapPair;
    }

    /**
     * Set name of the overlap port pair
     * @param overlapPair name of the overlap port pair
     */
    public void setOverlapPair(String overlapPair) {
        this._overlapPair = overlapPair;
    }

    /**
     * Get name of the assigned memory
     * @return name of the assigned memory
     */
    public String getAssignedMemoryName() {
       if(_assignedMemory==null)
        return getDefaultAssignedMemoryName();

       return _assignedMemory.getName();
    }

    /**
     * TODO check if underscores could be removed
     * @return
     */
    private String getDefaultAssignedMemoryName(){
      String defaultMemoryName = getName();
       /** if(_type==CSDFPortType.in)
            defaultMemoryName+="_input";
           if(_type==CSDFPortType.out)
            defaultMemoryName+="_output"; */
      return defaultMemoryName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /**
     * Checks if the rate lists are equal
     * @return true, if rate lists are equal and false otherwise
     */
    private boolean _isRatesEqual(Vector<IndexPair> otherPortRates){
        if(_rates.size()!=otherPortRates.size())
            return false;

        for(int i = 0; i< _rates.size(); i++){
            if(!(_rates.elementAt(i).equals(otherPortRates.elementAt(i))))
                return false;
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                     private variables                    ////
    /**
     * Description of a CSDF port rates, where each IndexPair
     * <Integer, Integer>  means <Rate,Number of rate repetitions>
     */
    @SerializedName("rates") Vector<IndexPair> _rates;
    /**
     * Id of CSDFNode (Actor) of port
     */
    private transient int _nodeId;
     /**
     * port unique identifier
     */
    @SerializedName("id")private int _id;

     /**
     * type of SDF Port
     */
    private transient CSDFPortType _type;

    /**
     * CSDFPort group : used for codeGeneration. Group of
     * CSDFPorts have the same rates and assigned to the same
     * internal memory unit
     */
    private transient Integer _group;

    /** description of the assigned node internal memory array */
    private @SerializedName("memory")MemoryUnit _assignedMemory = null;

    /**
     * Reading/writing start shift: used for overlapping processing
     */
    private transient Vector<IndexPair> _startTokens;

    /**
     * Flag, shows, that port is a special self-loop port, dependent on real inpur port
     * and resolving overlapping data
     */
    private @SerializedName("overlap_handler") boolean _overlapHandler = false;

    /**
     * Variable for a special self-loop port, dependent on other input port
     * (resolving overlapping data)
     */
    private @SerializedName("overlap_pair") String _overlapPair = null;
}

