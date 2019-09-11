package espam.datamodel.graph.cnn.operators;

/**
 * Internal buffer between two operators so that
 * input of dest operator is input of source operator
 */
public class InternalBuffer {

    /**
     * Create new internal buffer between two operators
     * @param src source operator
     * @param dst destination operator
     * @param bufferSize size of buffer between the operators
     */
    public InternalBuffer(Operator src, Operator dst, Integer bufferSize){
        _src = src;
        _dst = dst;
        _bufferSize = bufferSize;
    }

      /**
     *  Clone this NPort
     *
     * @return  a new instance of the NPort.
     */
    public Object clone() {
        try {
            InternalBuffer newObj = (InternalBuffer) super.clone();
            newObj.setSrc( (Operator) _src.clone() );
            newObj.setDst( (Operator) _dst.clone() );
            newObj._bufferSize = _bufferSize;
            return( newObj );
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

    public Operator getSrc() { return _src; }

    public Operator getDst() { return _dst; }

    public Integer getBufferSize() { return _bufferSize; }

    public void setSrc(Operator src) { this._src = src; }

    public void setDst(Operator dst) { this._dst = dst; }

    public void setBufferSize(Integer bufferSize) { this._bufferSize = bufferSize; }

    ///////////////////////////////////////////////////////////////////
    ////       private variables                                  ////

    private Operator _src;
    private Operator _dst;
    private Integer _bufferSize;
}
