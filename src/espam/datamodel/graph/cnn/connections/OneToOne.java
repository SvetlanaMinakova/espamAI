package espam.datamodel.graph.cnn.connections;

import espam.datamodel.EspamException;
import espam.datamodel.graph.cnn.Layer;
import espam.datamodel.graph.cnn.neurons.ConnectionDependent;

import java.util.Vector;
/**
 * TODO check safety: connection could change the properties of connected layers
 * Class of one-to-one connection, while each neuron of previous layer
 * is connected to one corresponding neuron of the next layer (unit connection matrix)
 * previous and next layer should have the same number of neurons to be connected via
 * this type of connection
 */
public class OneToOne extends Connection {
    /**
     * Constructor to create new OneToOne connection
     * @param src source layer
     * @param dest destination layer
     */
 public OneToOne(Layer src, Layer dest) {
     super(src,dest);
     setType(ConnectionType.ONETOONE);
     if(src.getNeuronsNum()!=dest.getNeuronsNum()){
         /** TODO: check!*/
         int minNeuronsNum = Math.min(src.getNeuronsNum(),dest.getNeuronsNum());
         src.setNeuronsNum(minNeuronsNum);
         dest.setNeuronsNum(minNeuronsNum);
       /**  System.out.println("one-to-one connection different neurons num for layers" +
                     src.getName() + "( " +src.getNeuronsNum() +" neurons) , "
                     + dest.getName() + "( " + dest.getNeuronsNum() +" neurons) ");*/
     }

    }

      /**
     * Get inputs number for destination layer
     * @return inputs number destination layer
     */
    public int getDstInputsNum(){
      //  if(getDest().getNeuron() instanceof ConnectionDependent)
            return getSrc().getNeuronsNum();
      //  return 1;
      //  return getChannels();
    }

    /**
     * Get outputs number for source layer
     * @return outputs number for source layer
     */
    public int getSrcOutputsNum(){ return 1;}

      /**
     * Return ids of elements connected to this element
     * @param elemId element id
     * @return ids of elements connected to this element
     */
      public Vector<Integer> getInputsForElement(int elemId) {
          Vector<Integer> inputs = new Vector<Integer>();
          inputs.add(elemId);
          return inputs;
      }

      /**
     *  Clone this Connection
     *
     * @return  a new reference to this Connection.
     */
    public Object clone() {
        OneToOne newObj = (OneToOne) super.clone();
        return (newObj);
    }

     /** Create a deep copy of the connection
     *  @param c connection to be copied
     */
    public OneToOne (OneToOne c){ super(c); }

    /**Get connection matrix
     * for this connection type matrix is square
     * @return connection matrix
     */
     public boolean[][] getConnectionMatrix()
      { int srcNeurons = getSrc().getNeuronsNum();
          boolean[][] connectionMatrix = new boolean[srcNeurons][srcNeurons];

              for(int i=0;i<srcNeurons;i++)
                  connectionMatrix[i][i]=true;

          return connectionMatrix;
      }
}