package espam.datamodel.graph.cnn.connections;

import espam.datamodel.graph.cnn.Layer;

import java.util.Vector;

/**
 * Class of all-to-all connection, while all neurons of previous layer
 * are connected to all neurons of the next layer (connection matrix consists of 1 only)
 */
public class AllToAll extends Connection {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Constructor to create new AllToAll connection
     * @param src source layer
     * @param dest destination layer
     */
     public  AllToAll(Layer src, Layer dest) {
          super(src,dest);
          setType(ConnectionType.ALLTOALL);
    }

    /**
     * Get inputs number for destination layer
     * @return inputs number destination layer
     */
    public int getDstInputsNum(){return getSrc().getNeuronsNum();};

    /**
     * Get outputs number for source layer
     * @return outputs number for source layer
     */
    public int getSrcOutputsNum(){ return getDest().getNeuronsNum();}

    /**
     * Return ids of elements connected to this element
     * @param elemId element id
     * @return ids of elements connected to this element
     */
      public Vector<Integer> getInputsForElement(int elemId) {
          Vector<Integer> inputs = new Vector<Integer>();
          for(int i=0;i<getSrc().getNeuronsNum();i++)
              inputs.add(i);
          return inputs;
      }

    /**
     *  Clone this Connection
     * @return  a new reference to this Connection.
     */
    public Object clone() {
        AllToAll newObj = (AllToAll) super.clone();
        return (newObj);
    }

    /** Create a deep copy of the connection
     *  @param c connection to be copied
     */
    public AllToAll (AllToAll c){ super(c); }

    /**
     * Get connection matrix
     * @return connection matrix
     */
      public boolean[][] getConnectionMatrix()
      { int srcNeurons = getSrc().getNeuronsNum();
        int snkNeurons = getDest().getNeuronsNum();
          boolean[][] connectionMatrix = new boolean[srcNeurons][snkNeurons];

          for(int j=0;j<snkNeurons;j++)
          {
              for(int i=0;i<srcNeurons;i++)
                  connectionMatrix[i][j]=true;
          }
          return connectionMatrix;
      }

}
