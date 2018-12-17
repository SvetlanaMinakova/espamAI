package espam.datamodel.graph.cnn.connections;

import com.google.gson.annotations.SerializedName;
import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;

import java.util.Vector;

public class Custom extends Connection {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

     /**
     * Create custom connection with only Layer ids
     * @param srcId source layer id
     * @param destId destination layer Id
     */
     public Custom(int srcId, int destId, String srcName, String destName, int neuronsFromSrc) {
        super(srcId,destId,srcName,destName, neuronsFromSrc);
        setType(ConnectionType.CUSTOM);
    }

    /**
     * Create custom connection with only Layer ids
     * @param srcId source layer id
     * @param destId destination layer Id
     */
     public Custom(int srcId, int destId, String srcName, String destName, boolean[][] matrix) {
        super(srcId,destId,srcName,destName,1);
        if(matrix!=null){
            setChannels(matrix.length);
            setMatrix(matrix);
        }
     setType(ConnectionType.CUSTOM);
    }


    /** Create a deep copy of the connection
     *  @param c connection to be copied
     */
    public Custom (Custom c){
        super(c);
        if(c._matrix!=null) {
            _matrix = new boolean[c._matrix.length][c._matrix[0].length];

            for (int j = 0; j < _matrix.length; j++) {
                for (int i = 0; i < _matrix[j].length; i++) {
                    _matrix[j][i] = c._matrix[j][i];
                }
            }
        }
    }

    /**
     * Create custom connection
     * @param src source layer
     * @param dest destination layer
     * @param customConnection custom connection matrix
     * @throws EspamException in case of bad custom connection shape
     */
     public Custom(Layer src, Layer dest, boolean[][] customConnection){
         super(src,dest);
         setMatrix(customConnection);
         setType(ConnectionType.CUSTOM);
    }

      /**
     *  Clone this Connection
     * @return  a reference to the Connection.
     */
     public Object clone() {
        Custom newObj = (Custom) super.clone();
        newObj.setMatrix(_matrix.clone());
        return( newObj );
    }

    /**
     * Return string description of custom connection
     * @return string description of custom connection
     */
    @Override
    public String toString() {
        return ConnectionType.CUSTOM.toString();
    }// +"\n"+getMatrixDescription() - if needed

    /**
      * Compares Custom connection with another object
      * @param obj Object to compare this Custom connection with
      * @return true if Custom connection is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        boolean isSuperParamsEqual = super.equals(obj);

        if(isSuperParamsEqual)
            return isMatrixEqual(((Custom) obj).getMatrix());

        return false;
       }

    /**
     * Get inputs number for destination layer
     * @return inputs number destination layer
     */
    public int getDstInputsNum(){ return _getTotalConnectionsNum(); }

    /**
     * Get inputs number for destination neuron
     * @return inputs number destination neuron
     */
    public int getDstNeuronInputsNum(int neuronId){ return _getTotalConnectionsNum(); }

    /**
     * Get outputs number for source layer
     * @return outputs number for source layer
     */
    public int getSrcOutputsNum(){ return _getTotalConnectionsNum(); }

      /**
     * Return ids of elements connected to this element
     * @param elemId element id
     * @return ids of elements connected to this element
     */
      public Vector<Integer> getInputsForElement(int elemId) {
          Vector<Integer> inputs = new Vector<Integer>();
          for (int j = 0; j < getDest().getNeuronsNum(); j++) {
              if(_matrix[elemId][j])
                  inputs.add(j);
          }

          inputs.add(elemId);
          return inputs;
      }

    /**
     * Return ids of elements connected to this element
     * @param elemId element id
     * @return ids of elements connected to this element
     */
      public Vector<Integer> getOutputsForElement(int elemId) {
          Vector<Integer> inputs = new Vector<Integer>();
          for (int i = 0; i < getSrc().getNeuronsNum(); i++) {
              if(_matrix[i][elemId])
                  inputs.add(i);
          }

          inputs.add(elemId);
          return inputs;
      }

    /**Get connection matrix
     * @return connection matrix
     */
    public boolean[][] getConnectionMatrix() { return _matrix; }

    /**Get custom connection matrix
     * @return custom connection matrix
     */
    public boolean[][] getMatrix() { return _matrix; }

    /**Set custom connection matrix
     * @param matrix custom connection matrix
     */
    public void setMatrix(boolean[][] matrix) { _matrix = matrix; }

    /**Get custom connection matrix description
     * @return custom connection matrix description
     */
    public String getMatrixDescription() {
        StringBuilder matrixDescription = new StringBuilder("");
        if(_matrix!=null) {
            for (int j = 0; j < _matrix.length; j++) {
                String row = "";
                for (int i = 0; i < _matrix[j].length; i++) {
                    if (_matrix[j][i])
                        row += "1";
                    else
                        row += "0";
                }
                matrixDescription.append(row + "\n");
            }
        }
        return matrixDescription.toString();
    }
    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /**Compares Custom connection matrix with another matrix
     * @param matrix matrix for comparison
     * @return true if matrices are equal and false otherwise
     */
    public boolean isMatrixEqual(boolean[][] matrix) {
        if (matrix == null || _matrix == null)
            return false;
        try {
             for (int j = 0; j < _matrix.length; j++) {
                 for (int i = 0; i < _matrix[j].length; i++) {
                     if (!_matrix[j][i]==matrix[j][i])
                         return false;
                    }
                }

            return true;
        }

        catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Get total number of connections, defined by matrix
     * @return total number of connections, defined by matrix
     */
    private int _getTotalConnectionsNum(){
        if(_matrix==null)
            return 0;
        int connections = 0;

        for (int j = 0; j < _matrix.length; j++) {
            for (int i = 0; i < _matrix[j].length; i++)
                  connections++;
            }
            return connections/2;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    /**
     * custom connection matrix
     */
    private transient boolean[][] _matrix;
}
