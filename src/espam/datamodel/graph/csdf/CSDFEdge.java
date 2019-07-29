package espam.datamodel.graph.csdf;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.Edge;
import espam.datamodel.graph.NPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.platform.Port;
import espam.parser.json.ReferenceResolvable;
import espam.visitor.CSDFGraphVisitor;

import java.util.Vector;

/**
 * An SDF Graph Edge
 */
public class CSDFEdge extends Edge {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create an SDF Edge with a name, an empty
     *  portList, infinite buffer and initial number of tokens = 0
     */
    public CSDFEdge (String name,int id) {
        super(name);
        setId(id);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(CSDFGraphVisitor x) {
        x.visitComponent(this);
    }

    /**
     *  Clone this CSDFEdge
     *
     * @return  a new instance of the CSDFEdge.
     */
    public Object clone() {
        CSDFEdge newObj = (CSDFEdge) super.clone();
        newObj.setId(_id);
        return( newObj );
    }

     /**
     *  Return a description of the edge.
     * @return  a description of the edge.
     */
    @Override
    public String toString() { return getName(); }

    /**
      * Compares SDFNode with another object
      * @param obj Object to compare this CSDFEdge with
      * @return true if CSDFEdge is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

        CSDFEdge edge = (CSDFEdge)obj;
        return _id == edge._id
              && this.getName().equals(edge.getName())
              && isSrcEqual(edge)
              && isDstEqual(edge);
       }

    /**
     * get unique edge id
     * @return unique edge id
     */
    public int getId() {
        return _id;
    }

    /**
     * set unique edge id
     * @param id unique edge id
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get Id of the source port
     * @return Id of the source port
     */
    public int[] getSrcId() {
        return _srcId;
    }
    /**
     * Set Id of the source port
     * @param srcId Id of the source port
     */
    public void setSrcId(int[] srcId) {
        this._srcId = srcId;
    }
    /**
     * Get Id of the sink port
     * @return Id of the sink port
     */
    public int[] getDstId() {
        return _dstId;
    }
    /**
     * Set Id of the sink port
     * @param dstId Id of the sink port
     */
    public void setDstId(int[] dstId) {
        this._dstId = dstId;
    }

     /**
     * Get source port
     * @return source port
     */
    public CSDFPort getSrc() {
        return _src;
    }
    /**
     * Set source port
     * @param src source port
     */
    public void setSrc(CSDFPort src) {
        _src = src;
        _srcId[0]=src.getNodeId();
        _srcId[1]=src.getId();
    }

    /**
     * Change destination node
     * @param newDst new destination
     */
    public void changeDst(CSDFNode newDst){
        //1. transfer out port
        CSDFPort outPort = getDst();
        CSDFNode oldDstNode = (CSDFNode) getDst().getNode();
        oldDstNode.removePort(outPort);
        outPort.setId(newDst.getNextPortId());
        newDst.addPort(outPort);

        //2.Transfer memory unit, associated with a port
        if(outPort.getAssignedMemory()!=null)
            newDst.addMemoryUnit(outPort.getAssignedMemory());

        //2.change dst id
        _dstId[0] = newDst.getId();
        _dstId[1] = outPort.getId();
    }

    /**
     * Change source node
     * @param newSrc new source
     */
    public void changeSrc(CSDFNode newSrc){

    }

    /**
     * Get dest port
     * @return dest port
     */
    public CSDFPort getDst() {
        return _dst;
    }
    /**
     * Set dest port
     * @param dst dest port
     */
    public void setDst(CSDFPort dst) {
        this._dst = dst;
        _dstId[0] = dst.getNodeId();
        _dstId[1] = dst.getId();
    }

    /**
     * SDF portList contains (one) source and (one) destination ports
     * @return portList contains (one) source and (one) destination ports
     */
    @Override
    public Vector<NPort> getPortList()
    { Vector<NPort> v = new Vector<NPort>();
        v.add(_src);
        v.add(_dst);
        return v;
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private methods                      ////

    /**
     * Compare source description of CSDFEdge with source description of another CSDFEdge
     * @param edge edge to compare with
     * @return true, if sources are equal
     * the reference on src SDFNode object is not included in comparison,
     * because outside of the SDFGraph it could not be resolved
     */
    private boolean isSrcEqual(CSDFEdge edge) {

        return  this._srcId[0]==edge._srcId[0]
                   && this._srcId[1]==edge._srcId[1];
       }

     /**
     * Compare destination description of CSDFEdge with destination description of another CSDFEdge
     * @param edge edge to compare with
     * @return true, if destinations are equal
     * the reference on dst SDFNode object is not included in comparison,
     * because outside of the SDFGraph it could not be resolved
     */
    private boolean isDstEqual(CSDFEdge edge) {

         return this._dstId[0]==edge._dstId[0]
                   && this._dstId[1]==edge._dstId[1];
       }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////
   /**
     * Unique edge id
     */
    @SerializedName("id")private int _id;
     /**
     * Id of the source node int[sourceNodeId,sourcePortId]
     */
     @SerializedName("src")private int[] _srcId = new int[2];
      /**
     * Id of the sink node int[destNodeId,destPortId]
     */
     @SerializedName("dst")private int[] _dstId = new int[2];

     /** Source port */
     private transient CSDFPort _src = null;

     /** Destination port*/
     private transient CSDFPort _dst = null;
}
