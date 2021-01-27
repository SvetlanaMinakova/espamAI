package espam.datamodel.graph.cnn.connections;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.neurons.MultipleInputsProcessor;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.CNNGraphVisitor;

import java.util.Vector;
/**
 * Abstract class of connection between two layers of Neural Network
 * @author Svetlana Minakova
 */
public abstract class Connection implements Cloneable{
     ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /**
     * Protected constructor, implements initialization operations common
     * for every rype of connection
     * @param src source layer of the connection
     * @param dest destination layer of the connection
     */
    protected Connection(Layer src, Layer dest) {
        setSrc(src);
        setSrcName(src.getName());
        setSrcId(src.getId());
        setDestId(dest.getId());
        setDest(dest);
        setDestName(dest.getName());
        setAutoChannelsNum();
    }
      /**
     * Protected constructor, implements initialization operations common
     * for every rype of connection
     * @param srcId id of source layer of the connection
     * @param destId id of destination layer of the connection
     */
    protected Connection(int srcId, int destId, String srcName, String destName, int neuronsFromSrc) {
        setSrcId(srcId);
        setDestId(destId);
        setSrcName(srcName);
        setDestName(destName);
        setChannels(neuronsFromSrc);
    }


    /**
     * Create a deep copy of the connection
     * @param c connection to be copied
     */
    protected Connection(Connection c){
        setSrcName(c._srcName);
        setDestName(c._destName);
        setType(c._type);
        setSrcId(c._srcId);
        setDestId(c._destId);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /**
     *  Clone this Connection
     * @return  a new reference to the Connection.
     */
    @SuppressWarnings(value={"unchecked"})
    public Object clone() {
        try {
            Connection newObj =(Connection) Connection.super.clone();
            newObj.setSrc((Layer)_src.clone());
            newObj.setSrcName(_srcName);
            newObj.setDest((Layer)_dest.clone());
            newObj.setDestName(_destName);
            newObj.setType(_type);
            newObj.setSrcId(_srcId);
            newObj.setDestId(_destId);
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

      /**
      * Compares Connection with another object
      * @param obj Object to compare this Connection with
      * @return true if Connection is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

        Connection con = (Connection)obj;
        return this.getType().equals(con.getType())
              && isSrcEqual(con)
              && isDstEqual(con);
       }

    /**
     * Create a deep copy of the connection
     * @param c connection to be copied
     */
    public static Connection copyConnection(Connection c){
        if(c instanceof AllToAll)
            return new AllToAll((AllToAll)c);
        if(c instanceof OneToOne)
            return new OneToOne((OneToOne)c);
        if(c instanceof OneToAll)
            return new OneToAll((OneToAll)c);
        if(c instanceof AllToOne)
            return new AllToOne((AllToOne)c);
        if(c instanceof Custom)
            return new Custom((Custom)c);

        System.err.println("Connection copy error: unknown connection type");
        return null;
    }

    /**
     * Get inputs number for destination layer
     * @return inputs number for  destination layer
     */
    public abstract int getDstInputsNum();

    /**
     * Get outputs number for source layer
     * @return outputs number for source layer
     */
    public abstract int getSrcOutputsNum();

    /**
     * Get connection matrix of the connection
     * @return connection matrix of the connection
     */
    public abstract boolean[][] getConnectionMatrix();

    /**
     * Return ids of elements connected to this element
     * @param elemId element id
     * @return ids of elements connected to this element
     */
    public abstract Vector<Integer> getInputsForElement(int elemId);

    /**
     * Reset source layer and all related parameters
     * @param newSrc new source
     */
    public void changeSrc(Layer newSrc){
        if(_dest.getNeuron() instanceof MultipleInputsProcessor){
            Vector<Layer> destInputOwners = ((MultipleInputsProcessor) _dest.getNeuron()).getInputOwners();
            Integer oldsrcId =  destInputOwners.indexOf(_src);
            if(oldsrcId!=-1){
                destInputOwners.removeElement(_src);
                destInputOwners.insertElementAt(newSrc,oldsrcId);
            }
        }

        this._src = newSrc;
        this._srcName = newSrc.getName();
        this._srcId = newSrc.getId();
        setAutoChannelsNum();
    }
    /**
     * Reset destination layer and all related parameters
     * @param newDest new destination
     */
    public void changeDest(Layer newDest){
        this._dest = newDest;
        this._destName = newDest.getName();
        this._destId = newDest.getId();
    }

    /**
     * Set channels number automatically
     */
    public void setAutoChannelsNum(){
        if(_src==null) {
            _channels = 0;
            return;
        }
        if(this instanceof OneToOne || this instanceof OneToAll) {
            _channels = 1;
            return;
        }
        if(this instanceof Custom) {
            _channels = ((Custom) this).getSrcOutputsNum();
            return;
        }

        /** all-to-all or one-to-all*/
        _channels =_src.getNeuronsNum();
    }


    /**
     * Get source layer of the connection
     * @return source layer of the connection
     */
    public Layer getSrc() { return _src; }

    /**
     * Set source layer of the connection
     * @param src source layer of the connection
     */
    public void setSrc(Layer src) {
        this._src = src;
    }

    /**
     * Get destination layer of the connection
     * @return destination layer of the connection
     */
    public Layer getDest() { return _dest; }

    /**
     * Set destination layer of the connection
     * @return destination layer of the connection
     */
    public void setDest(Layer dest) {
        this._dest = dest;
    }

    /**
     * Get type of the connection
     * @return type of the connection
     */
    public ConnectionType getType() { return _type; }

    /**
     * Set type of the connection
     * @param type type of the connection
     */
    public void setType(ConnectionType type) {
        this._type = type;
    }

    /**
     * Get name of the destination Layer
     * @return name of the destination Layer
     */
    public String getDestName() { return _destName; }

    /**
     * Set name of the destination Layer
     * @param destName name of the destination Layer
     */
    public void setDestName(String destName) { this._destName = destName; }

     /**
     * Get id of the destination Layer
     * @return id of the destination Layer
     */
    public int getDestId() { return _destId; }

    /**
     * Set id of the destination Layer
     * @param destId  id of the destination Layer
     */
    public void setDestId(int destId) { this._destId = destId; }

     /**
     * Get name of the source Layer
     * @return name of the sourceLayer
     */
    public String getSrcName() { return _srcName; }

    /**
     * Set name of the source Layer
     * @param srcName  name of the sourceLayer
     */
    public void setSrcName(String srcName) { this._srcName = srcName; }

     /**
     * Get id of the source Layer
     * @return id of the sourceLayer
     */
    public int getSrcId() { return _srcId; }

     /**
     * Set id of the source Layer
     * @param srcId  id of the sourceLayer
     */
    public void setSrcId(int srcId) { this._srcId = srcId; }

    /**
     * Get number of neurons coming from
     * source layer to destination layer through the connection
     * @return number of neurons, coming from
     * source layer to destination layer through the connection
     */
    public int getChannels() { return _channels; }

      /**
     * Set number of neurons coming from src thought the connection
     * @return number of neurons, coming from src through the connection
     */
    public void setChannels(int channels) {
        this._channels = channels;
    }

    /**
     * Return description of the connection
     * @return description of the connection
     */
    @Override
    public String toString() {
        return getType().toString();
    }


     ///////////////////////////////////////////////////////////////////////
    ////                         private methods                       ////
       /**
     * Compare source description of Connection with source description
     * of another Connection
     * @param connection connection to compare with
     * @return true, if sources are equal
     * the reference on src Layer object is not included in comparison,
     * because outside of the CNN graph it could not be resolved
     */
        private boolean isSrcEqual(Connection connection) {
        return  this._srcId==connection._srcId
                   && this._srcName.equals(connection._srcName);
       }

       /**
     * Compare destination description of Connection with destination description
     * of another Connection
     * @param connection connection to compare with
     * @return true, if destinations are equal
     * the reference on dst Layer object is not included in comparison,
     * because outside of the CNN graph it could not be resolved
     */
    private boolean isDstEqual(Connection connection) {
        return  this._destId==connection._destId
                   && this._destName.equals(connection._destName);
    }

    /**************************************************
     **** POWER/PERFORMANCE/MEMORY evaluation
     *************************************************/

    public void set_memEval(double _memEval) { this._memEval = _memEval; }

    public void set_timeEval(double _timeEval) { this._timeEval = _timeEval; }

    public void set_energyEval(double _energyEval) { this._energyEval = _energyEval; }

    public void set_energyEvalJoules(double _energyEvalJoules) { this._energyEvalJoules = _energyEvalJoules; }

    public double get_timeEval() {
        return _timeEval;
    }

    public double get_memEval() { return _memEval; }

    public double get_energyEval() { return _energyEval; }

    public double get_energyEvalJoules() { return _energyEvalJoules; }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    /**
     * source layer of the connection
     */
    private transient Layer _src;

    /**
     * destination layer of the connection
     */
    private transient Layer _dest;

    /**
     * Type of the connection
     */
    @SerializedName("type")private ConnectionType _type;

    /** connection source name*/
    @SerializedName("src") private String _srcName;

    /** connection destination name*/
    @SerializedName("dest")private String _destName;

    /** connection source id*/
    @SerializedName("srcId")private int _srcId;

    /** connection destination id*/
    @SerializedName("destId")private int _destId;

    /** number of channels = number of inputs,
     * coming from src layer to destination layer*/
    @SerializedName("channels")private int _channels = 1;

    /** memory evaluation*/
    @SerializedName("mem_eval")private double _memEval = 0.0;

    /** time evaluation*/
    @SerializedName("time_eval")private double _timeEval = 0.0;

    /** energy evaluation*/
    @SerializedName("energy_eval")private double _energyEval = 0.0;

    /** energy evaluation*/
    @SerializedName("energy_eval_joules")private double _energyEvalJoules = 0.0;
}
