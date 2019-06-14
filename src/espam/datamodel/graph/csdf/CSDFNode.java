package espam.datamodel.graph.csdf;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.Node;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.visitor.CSDFGraphVisitor;

import java.util.Vector;
import java.util.Iterator;

   public class CSDFNode extends Node {
     ///////////////////////////////////////////////////////////////////
     ////                         public methods                    ////

    /**
     * Constructor to create an SDF Node (Actor) with a name, an empty
     * portList and production and consumption rates=0
     */
    public CSDFNode(String name,int id) {
        super(name);
        setId(id);
    }

    /**
     * Create a full copy of the node
     * @return node's copy
     */
    public CSDFNode copy() {
        CSDFNode nodeCopy = new CSDFNode(getName(),_id);
        nodeCopy.setPortList(getPortList());
        nodeCopy.setOperation(_operation);
        nodeCopy.setGroup(_group);
        nodeCopy.setLength(_length);
        Vector<MemoryUnit> memoryUnits = new Vector<>();
        for (MemoryUnit mu: _memoryUnits)
            memoryUnits.add(new MemoryUnit(mu));
        nodeCopy.setMemoryUnits(memoryUnits);
        nodeCopy.setKernelsNum(_kernelsNum);
        return nodeCopy;
    }


    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CSDFGraphVisitor x) { x.visitComponent(this); }

    /**
     *  Clone this CSDFNode
     *
     * @return  a new instance of the CSDFNode.
     */
    public Object clone() {
        CSDFNode newObj = (CSDFNode) super.clone();
        newObj.setId(_id);
        newObj.setOperation(_operation);
        newObj.setGroup(_group);
        /** port ids remain the same in the model copy,
         *  so list of overlap couples processors is just cloned*/
        newObj.setMemoryUnits(_memoryUnits);
        newObj.setLength(_length);
        newObj.setKernelsNum(_kernelsNum);
       // newObj.setInternalMemorySize((Tensor)_internalMemorySize.clone());
        return (newObj);
        
    }

       /**
        * Get sdf analogue of the node
        * @return sdf analogue of the node
        */
    public  CSDFNode getSDFNode(){
        CSDFNode sdfNode = new CSDFNode(getName(),_id);
       /** for(CSDFPort inport:getNonOverlapHandlingInPorts()){
            sdfNode.addPort(inport.getSDFPort());
        }

        for (CSDFPort outport:getNonOverlapHandlingOutPorts()){
            sdfNode.addPort(outport.getSDFPort());
        } */

       for(NPort port:getPortList()){
               sdfNode.addPort(((CSDFPort) port).getSDFPort());
       }

        sdfNode.setOperation(_operation);
       if(_group!=null)
        sdfNode.setGroup(_group);
        sdfNode.setLength(1);
        sdfNode.setRepetitions(1);
        Vector<MemoryUnit> memoryUnits = new Vector<>();
        sdfNode.setMemoryUnits(memoryUnits);
        sdfNode.setKernelsNum(_kernelsNum);

        return sdfNode;
    }

     /**
     * Return description of the CSDFNode (Actor).
     * @return description of the CSDFNode (Actor).
     */
    @Override
    public String toString() { return "CSDFNode: " + getName(); }

       /**
        * Compares CSDFNode with another object
        * @param obj Object to compare this CSDFNode with
        * @return true if CSDFNode is equal to the object and false otherwise
        */
       @Override
       public boolean equals(Object obj) {
           if (obj == this) {
               return true;
           }
           if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

           CSDFNode node = (CSDFNode)obj;
           return _id == node._id
                   && this.getName().equals(node.getName())
                   && this.isPortListEqual(node.getPortList());
       }

     /**
     * Removes a port from the CSDFNode (Actor) if it exists
     * @param port port to be removed
     */
     public void removePort(CSDFPort port) {
         getPortList().remove(port);
     }


    /**
     *  Get the input ports of this CSDFNode.
     * @return  the input ports
     */
    public Vector<CSDFPort> getInPorts() {
        Vector v = new Vector();

        Iterator i = getPortList().iterator();
        while ( i.hasNext() ) {
            CSDFPort port = (CSDFPort) i.next();
            if( port.getType().equals(CSDFPortType.in) ) {
                v.add( port );
            }
        }
        return v;
    }

      /**
     * Get input ports of the CSDFNode, which are not overlap handlers
     * @return  input ports of the CSDFNode, which are not overlap handlers
     */
    public Vector<CSDFPort> getOverlapHandlingInPorts() {
        Vector<CSDFPort> inports = new Vector<>();
        for(CSDFPort inport: getInPorts()) {
            if(inport.isOverlapHandler())
                inports.add(inport);
        }

        return inports;
    }

    /**
     * Get input ports of the CSDFNode, which are not overlap handlers
     * @return  input ports of the CSDFNode, which are not overlap handlers
     */
    public Vector<CSDFPort> getNonOverlapHandlingInPorts() {
        Vector<CSDFPort> inports = new Vector<>();
        for(CSDFPort inport: getInPorts()) {
            if(!inport.isOverlapHandler())
                inports.add(inport);
        }

        return inports;
    }

     /**
     * Get number of input ports of the CSDFNode, which are not overlap handlers
     * @return  number of input ports of the CSDFNode, which are not overlap handlers
     */
    public int getNonOverlapHandlingInPortsNum() {
        int nonOverlapPortsNum = 0;

        for(CSDFPort inport: getInPorts()) {
            if(!inport.isOverlapHandler())
                nonOverlapPortsNum++;
        }

        return nonOverlapPortsNum;
    }

    /**
     * Get the output ports of this CSDFNode.
     * @return  the output ports
     */
    public Vector<CSDFPort> getOutPorts() {
        Vector v = new Vector();

        Iterator i = getPortList().iterator();
        while( i.hasNext() ) {
            CSDFPort port = (CSDFPort) i.next();
            if(  port.getType().equals(CSDFPortType.out) ) {
                v.add(port);
            }
        }
        return v;
    }

     /**
     *  Get the overlapping-handlers output ports of this CSDFNode.
     * @return overlapping-handlers output ports of this CSDFNode.
     */
    public Vector<CSDFPort> getOverlapOutPorts() {
        Vector v = new Vector();
        Iterator i = getOutPorts().iterator();
        while ( i.hasNext() ) {
            CSDFPort port = (CSDFPort) i.next();
            if( port.isOverlapHandler() ) {
                v.add( port );
            }
        }
        return v;
    }


    /**
     * Get input ports of the CSDFNode, which are not overlap handlers
     * @return  input ports of the CSDFNode, which are not overlap handlers
     */
    public Vector<CSDFPort> getNonOverlapHandlingOutPorts() {
        Vector<CSDFPort> outports = new Vector<>();
        for(CSDFPort outport: getOutPorts()) {
            if(!outport.isOverlapHandler())
                outports.add(outport);
        }

        return outports;
    }

       /**
        * Returns CSDFPort with specified id
        * @param id unique identifier(id) of an SDF port
        * @return CSDFPort with specified id
        */
    public CSDFPort getPort(int id) {
        Iterator i = getPortList().iterator();
        while( i.hasNext() ) {
            CSDFPort port = (CSDFPort) i.next();
            if( port.getId()==id) {
                return port;
            }
        }
        return null;
    }

       /**
     * Get number of CSDFNode ports
     * @return number of CSDFNode ports
     */
    public int countPorts(){
        return getPortList().size();
    }

    /**
     * Generates new input port name automatically without port existence checking (fast but unsafe)
     * @return new Port name
     */

    public String getNextInPortName() {
        return "IP" + getInPorts().size();
    }

    /**
     * Generates new output port name automatically without port existence checking (fast but unsafe)
     * @return new Port name
     */
    public String getNextOutPortName() {
        return "OP" + getOutPorts().size();
    }
    /**
     * get next port id
     */
    public int getNextPortId() {
        return getPortList().size();
    }


    /**
     * Checks if the port already exist
     * @return true, if the port not exist and false otherwise
     */
    public boolean ifPortNotExist(String portname) {
        return (getPort(portname) == null);
    }

    /**
    * Add new port to the CSDFNode
    */
    public void addPort(CSDFPort port) {
        port.setNode(this);
        getPortList().add(port);
    }


     /**
      * CSDFNode is specified by unique id and have a [non-unique] name.
      * In case of application, where the CSDFNode name should be unique,
      * used unique structure (CSDFNode)name_(CSDFNode)Id
      */
       public String getUniqueName() {
           String uniqueName = super.getName();
           uniqueName+="_";
           uniqueName+=getId();
           return uniqueName;
     }

      /**
     * Get node unique identifier
     * @return node unique identifier
     */
    public int getId() { return _id; }

     /**
     * Set node unique identifier
     * @param id node unique identifier
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
    * Get CSDFNode group, if any. If SDF Node have no group, null is returned.
    * @return CSDFNode group or null
    */
    public String getGroup() { return _group; }

    /**
     * Set CSDFNode group
     * @param group CSDFNode group
     */
    public void setGroup(String group) { this._group = group; }

    /**
     * Get node operation name
     * @return node operation name
     */
    public String getOperation() { return _operation; }

     /**
     * Set node operation name
     * @param operationName node operation name
     */
    public void setOperation(String operationName) { this._operation = operationName; }

    /** Get number of operation repetitions per firing
        * @return number of operation repetitions per firing
        */
    public int getOperationRepetitionsNumber() {
        return _operationRepetitionsNumber;
    }

    /**
    * Set  number of operation repetitions per firing
    * @param operationRepetitionsNumber  number of operation repetitions per firing
    */
    public void setOperationRepetitionsNumber(int operationRepetitionsNumber) {
           this._operationRepetitionsNumber = operationRepetitionsNumber;

    }

    /**
     * Get firing sequence length
     * @return firing sequence length
     */
    public int getLength() { return _length; }

    /**
     * Set firing sequence length
     * @param length  firing sequence length
     */
    public void setLength(int length) {
           this._length = length;
       }

    /**
    * Set node length automatically, taking into account
    * node rates
    */
    private void setAutoLength(){
        int autoLen = getMaxPhases();
        setLength(autoLen);
    }

    /**
     * Get number of node repetitions
     * @return number of node repetitions
      */
    public int getRepetitions() { return _repetitions; }

    /**
    * Set number of node repetitions
    * @param repetitions number of node repetitions
    */
    public void setRepetitions(int repetitions) {
        this._repetitions = repetitions;
    }

    /**
     * Get number of kernels, processing input inside of the node
     * @return number of kernels, processing input inside of the node
     */
    public int getKernelsNum() {
           return _kernelsNum;
       }

    /**
     * Set number of kernels, processing input inside of the node
     * @return number of kernels, processing input inside of the node
     */
       public void setKernelsNum(int kernelsNum) {
           this._kernelsNum = kernelsNum;
       }

       /**
        * Align rates according ro calculated length property
        */
       public void alignRatesLength(int maxPhases){
      //     setAutoLength();
           setLength(maxPhases);
           Vector<IndexPair> curRates;
           int dif;
           int curPhases = 0;
           /** align reading rates*/
           for(CSDFPort inport: getInPorts()){
               curRates = inport._rates;
               for (IndexPair rate: curRates){
                   curPhases+=rate.getSecond();
               }
               dif = _length - curPhases;
               if(dif>0)
                   inport.getRates().add(new IndexPair(0,dif));

               curPhases =0;
           }
           /** align writing rates*/
           for(CSDFPort outport: getOutPorts()){
               curRates = outport._rates;
               for (IndexPair rate: curRates){
                   curPhases+=rate.getSecond();
               }
               dif = _length - curPhases;
               if(dif>0) {
                   if(outport.isOverlapHandler())
                       outport.getRates().add(new IndexPair(0, dif));
                   else
                       outport.getRates().insertElementAt(new IndexPair(0, dif), 0);
               }
               curPhases =0;
           }
       }

       /**
        * Align rates according ro calculated length property
        */
    public void alignRatesLength(){
        setAutoLength();
        Vector<IndexPair> curRates;
        int dif;
        int curPhases = 0;
        /** align reading rates*/
        for(CSDFPort inport: getInPorts()){
            curRates = inport._rates;
            for (IndexPair rate: curRates){
                curPhases+=rate.getSecond();
            }
            dif = _length - curPhases;
            if(dif>0)
                inport.getRates().add(new IndexPair(0,dif));

            curPhases =0;
        }
        /** align writing rates*/
        for(CSDFPort outport: getOutPorts()){
            curRates = outport._rates;
            for (IndexPair rate: curRates){
                curPhases+=rate.getSecond();
            }
            dif = _length - curPhases;
            if(dif>0) {
                if(outport.isOverlapHandler())
                    outport.getRates().add(new IndexPair(0, dif));
                else
                    outport.getRates().insertElementAt(new IndexPair(0, dif), 0);
            }
            curPhases =0;
        }
    }

    /** get max number of phases of this node
        * @return max number of phases of this node
        */
    private int getMaxPhases(){
        int maxPhases = 0;
        int curPhases = 0;
        Vector<IndexPair> curRates;
        for(CSDFPort inport: getInPorts()){
            curRates = inport._rates;
            for (IndexPair rate: curRates){
                curPhases+=rate.getSecond();
            }
            if(curPhases>maxPhases)
                maxPhases = curPhases;
            curPhases =0;
        }

        for(CSDFPort outport: getOutPorts()){
            curRates = outport._rates;
            for (IndexPair rate: curRates){
                curPhases+=rate.getSecond();
            }
            if(curPhases>maxPhases)
                maxPhases = curPhases;
            curPhases = 0;
        }
        return maxPhases;
    }


     /////////////////////////////////////////////////////////////////////
    ////                    memory units processing                  ////

       /**
        * calculate the state size of the node by summing up
        * all the memory units, owned by a node
        */
       public void getStateSize(){

       }

       /**
        * Add new memory unit description to the node
        * @param mu memory unit, related to the node
        */
       public void addMemoryUnit(MemoryUnit mu){
           _memoryUnits.add(mu);
       }

       /**
        * Assign existing memory unit to existing CSDF node port
        * @param muName name of the existing memory unit
        * @param port name of the existing port
        */
       public void assignMemoryUnit(String muName, CSDFPort port){
           MemoryUnit mu = getMemoryUnit(muName);
           port.setAssignedMemory(mu);

       }

       /**
        * Get memory unit by name
        * @param muName name of th memory unit
        * @return memory unit, found by name or null
        */
       public MemoryUnit getMemoryUnit(String muName){
           if(_memoryUnits==null)
               return null;
           for(MemoryUnit mu: _memoryUnits){
               if(mu.getName().equals(muName))
                   return mu;
           }
           return null;
       }

       /**
        * Get all memory units, assigned to the node
        * @return all memory units, assigned to the node
        */
       public Vector<MemoryUnit> getMemoryUnits() {
           return _memoryUnits;
       }

       /**
        * Get memory units assigned to const parameters
        * @return list of memory units assigned to const parameters
        */
       public Vector<MemoryUnit> getUnitParams() {
           Vector<MemoryUnit> constParams = new Vector<>();
           for(MemoryUnit mu:_memoryUnits) {
               if(mu.isUnitParam()){
                   constParams.add(mu);
               }
           }
           return constParams;
       }

       /**
        * Assing vector of memory units to the node
        * @param memoryUnits vecor of memory units to be assigned to the node
        */
       public void setMemoryUnits(Vector<MemoryUnit> memoryUnits) {
           this._memoryUnits = memoryUnits;
       }

     /////////////////////////////////////////////////////////////////////
    ////                         private methods                     ////
        /**
        * Compares portLists of CSDFNodes with given portList
        * @param portList given list of ports
        * @return true, if portLists are equal and false otherwise
        */
       private boolean isPortListEqual(Vector<NPort> portList)
       {
           if(getPortList().size()!=portList.size())
               return false;

        Iterator i = portList.iterator();
        Vector<NPort> thisPortList = getPortList();

        while ( i.hasNext() ) {
            CSDFPort port = (CSDFPort) i.next();
            if(!thisPortList.contains(port)) {
                return false;
            }
        }
        return true;
       }


    /////////////////////////////////////////////////////////////////////
    ////                         private variables                   ////
    /** Unique node id */
    @SerializedName("id")private int _id;

    /** node repetitions (firings) number */
    @SerializedName("repetitions")private int _repetitions = 0;

    /** firing sequence length */
    @SerializedName("length")private int _length;

    /** executable operation*/
    @SerializedName("operation")private String _operation;

    /**operation repetitions number within one node firing */
    @SerializedName("op_repetitions_num")private int _operationRepetitionsNumber = 1;

     /**
      * number of operation instances, running within CSDF node.
      * Equal to number of neurons in layer-based model of CNN
      * */
    @SerializedName("kernels_num")private int _kernelsNum = 1;

    /**Flag, shows if an SDF port process overlapping*/
    @SerializedName("memory_units")private Vector<MemoryUnit> _memoryUnits = new Vector<>();

    /**SDF Node group (if any)*/
    @SerializedName("group")private String _group = null;
}
