package espam.datamodel.graph.cnn.connections;

import espam.datamodel.graph.cnn.Layer;

import java.util.Vector;
/**
 * Class of one-to-all connection, while one (first) neuron of previous layer
 * is connected to all neurons of the next layer
 */
public class OneToAll extends Connection{
     /**
     * Constructor to create new OneToAll connection
     * @param src source layer
     * @param dest destination layer
     */
    public  OneToAll(Layer src, Layer dest) {
          super(src,dest);
          setType(ConnectionType.ONETOALL);
    }

    /** Create a deep copy of the connection
     *  @param c connection to be copied
     */
    public OneToAll (OneToAll c){ super(c); }

    /**
     * Get inputs number for destination layer
     * @return inputs number destination layer
     */
    public int getDstInputsNum(){return 1;};

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
          inputs.add(0);
          return inputs;
      }

    /**
     *  Clone this Connection
     *
     * @return  a new reference to this Connection.
     */
    public Object clone() {
        OneToAll newObj = (OneToAll) super.clone();
        return (newObj);
    }

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
                  connectionMatrix[0][j]=true;
          }
          return connectionMatrix;
      }

}
