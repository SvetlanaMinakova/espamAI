package espam.datamodel.graph.sbrs.supergraph;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.graph.cnn.connections.Connection;
import espam.datamodel.graph.csdf.datasctructures.Tensor;

public class SBRSConnection {
    ///////////////////////////////////////////////////////////////////
    ////                         public functions                 ////

    /**Constructor to create new SBRS connection from a CNN connection
     * @param src source layer (in the SBRS MoC)
     * @param dst destination layer (in the SBRS MoC)
     * @param parent parent connection (in scenario)
     * @param duplicateId: SBRS MoC allows to have more than 1 connection with
     * the same src and dst. The connections with the same src and dst are called
     * duplicates.
     *         - if duplicateId = 0, this is the original connection
     *         - If duplicateId > 0, it specifies the id of the connection duplicate
     */
    public SBRSConnection(Connection parent, SBRSLayer src, SBRSLayer dst, Integer duplicateId) {
        _parent = parent;
        _src = src;
        _dst = dst;
        _bufferSize = (long)parent.getDest().getOutputFormat().getElementsNumber();
        _duplicateId = duplicateId;
    }

    public void reuseForConnection(Connection newSupportedConnection){
        Tensor srcOutput = newSupportedConnection.getSrc().getOutputFormat();
        _bufferSize = Math.max(_bufferSize, srcOutput.getElementsNumber());
    }

    /**
     * Compares parameters of this SBRS connection with given parameters
     * to decide, whether the SBRS connection can be reused to capture
     * these parameters
     * @param src source SBRS layer
     * @param dst destination SBRS layer
     * @param duplicateId duplicate Id of the connection (see SBRS connection _duplicateId)
     * @return true, if the SBRS connection can be reused to capture given parameters,
     * and false otherwise
     */
    public boolean reusableForConnection(SBRSLayer src, SBRSLayer dst, Integer duplicateId){
        boolean srcMatches = getSrc().equals(src);
        boolean dstMatches = getDst().equals(dst);
        boolean duplicateIdMatches = getDuplicateId() == duplicateId;
        return srcMatches && dstMatches && duplicateIdMatches;
    }


    /**
     * Compares SBRSLayer  with another object
     * @param obj Object to compare this SBRSLayer with
     * @return true if SBRSLayer is equal to the object and false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != this.getClass()) { return false; }

        SBRSConnection connection = (SBRSConnection) obj;
        return connection.getSrc().equals(getSrc()) &&
                connection.getDst().equals(getDst()) &&
                connection._duplicateId == _duplicateId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    getters and setters                   ////

    public SBRSLayer getSrc() { return _src; }

    public SBRSLayer getDst() { return _dst; }

    public Integer getDuplicateId() {return _duplicateId; }

    public Long getBufferSize() {return _bufferSize;}

    /**************************************************
     **** Print
     *************************************************/

    public void printDetails(){
            System.out.println("  " + _src.getName() + " --> " + _dst.getName() +
                    ", buffer: " + _bufferSize + " tokens");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                ////

    /** A CNN Edge, used for creation of the SBRS MoC layer*/
    @SerializedName("parent")private Connection _parent;
    @SerializedName("src")private SBRSLayer _src;
    @SerializedName("dst")private SBRSLayer _dst;
    @SerializedName("bufferSize")private Long _bufferSize;

    /** SBRS MoC allows to have more than 1 connection with the same src and dst.
     * The connections with the same src and dst are called duplicates.
     *         - if duplicateId = 0, this is the original connection
     *         - If duplicateId > 0, it specifies the id of the connection duplicate*/
    @SerializedName("duplicateId")private Integer _duplicateId = 0;
}
