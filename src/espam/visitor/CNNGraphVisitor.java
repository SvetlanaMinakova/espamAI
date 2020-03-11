package espam.visitor;
import espam.datamodel.graph.cnn.*;
import espam.datamodel.graph.cnn.connections.*;
import espam.datamodel.graph.cnn.neurons.arithmetic.Arithmetic;
import espam.datamodel.graph.cnn.neurons.cnn.CNNNeuron;
import espam.datamodel.graph.cnn.neurons.cnn.Convolution;
import espam.datamodel.graph.cnn.neurons.cnn.Pooling;
import espam.datamodel.graph.cnn.neurons.generic.GenericNeuron;
import espam.datamodel.graph.cnn.neurons.simple.Data;
import espam.datamodel.graph.cnn.neurons.simple.DenseBlock;
import espam.datamodel.graph.cnn.neurons.simple.Dropout;
import espam.datamodel.graph.cnn.neurons.simple.NonLinear;
import espam.datamodel.graph.cnn.neurons.transformation.Concat;
import espam.datamodel.graph.cnn.neurons.normalization.LRN;
import espam.datamodel.graph.cnn.neurons.transformation.Reshape;
import espam.datamodel.graph.cnn.neurons.transformation.Transpose;
import espam.datamodel.graph.cnn.neurons.transformation.Upsample;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
/**
 *  This class is an abstract class for a visitor that is used to generate
 *  Neural Network Graph description.
 *
 *  @author minakovas
 */
public class CNNGraphVisitor extends GraphVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call CNN visitor
     * @param dnn dnn to be visited
     * @param dir directory for  file corresponding to visited dnn
     */
     public static void callVisitor(Network dnn, String dir){}



    ///////////////////////////////////////////////////////////////////
    ////                    main elements visit                    ///

      /**
     *  Visit a Network component.
     *
     * @param  x A Visitor Object.
    */
    public void visitComponent(Network x) { }

     /**
     *  Visit a Layer component.
     *
     * @param  x A Visitor Object.
    */
    public void visitComponent(Layer x) { }

         /**
     *  Visit a Layer component.
     *
     * @param  x A Visitor Object.
    */
    public void visitComponent(Layer x, String dir) { }

     /**
     *  Visit a Neuron component.
     *  as Neuron is an abstract object, by default only
     *  real instances of the neuron are visited
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Neuron x) {
        if (x instanceof Arithmetic) {
            visitComponent((Arithmetic) x);
            return;
        }

          if (x instanceof Data) {
            visitComponent((Data)x);
            return;
        }

        if (x instanceof DenseBlock) {
            visitComponent((DenseBlock) x);
            return;
        }

        if (x instanceof Dropout) {
            visitComponent((Dropout) x);
            return;
        }

        if (x instanceof CNNNeuron) {
            visitComponent((CNNNeuron)x);
            return;
        }

        if (x instanceof Concat) {
            visitComponent((Concat) x);
            return;
        }


        if (x instanceof GenericNeuron) {
            visitComponent((GenericNeuron) x);
            return;
        }

        if (x instanceof LRN) {
            visitComponent((LRN) x);
            return;
        }

        if (x instanceof NonLinear) {
            visitComponent((NonLinear) x);
            return;
        }

        if (x instanceof Reshape) {
            visitComponent((Reshape) x);
            return;
        }

        if(x instanceof Upsample) {
            visitComponent((Upsample) x);
            return;
        }

        if( x instanceof Transpose) {
            visitComponent((Transpose)x);
            return;
        }
    }

           /**
     *  Visit a Tensor component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Tensor x) { }


    ///////////////////////////////////////////////////////////////////
    ////                      connections visit                    ///
    /**
     *  Visit a Connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Connection x) { }

     /**
     *  Visit a custom Connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Custom x) { }

      /**
     *  Visit a AllToAll connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(AllToAll x) { }

    /**
     *  Visit a OneToOne connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(OneToOne x) { }

    /**
     *  Visit a AllToOne connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(AllToOne x) { }

    /**
     *  Visit a OneToAll connection component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(OneToAll x) { }


    ///////////////////////////////////////////////////////////////////
    ////                      neurons visit                    ///

    /**
     *  Visit an Add component.
     * @param  x A Visitor Object.
    */
    public void visitComponent( Arithmetic x ){ }

    /**
     *  Visit a Concat component.
     * @param  x A Visitor Object.
    */
    public void visitComponent( Concat x ){ }

    /**
     *  Visit a Data component.
     * @param  x A Visitor Object.
    */
    public void visitComponent( Data x ){ }

    /**
     *  Visit a Data component.
     * @param  x A Visitor Object.
    */
    public void visitComponent( DenseBlock x ){ }

      /**
     *  Visit a Dropout component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(Dropout x) { }

     /**
     *  Visit a Reshape component.
     * @param  x A Visitor Object.
    */
    public void visitComponent( Reshape x ){ }

    /**
     *  Visit a CNNNeuron component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CNNNeuron x) { }

    /**
     *  Visit a Convolution component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(Convolution x) { }

    /**
     *  Visit a Pooling component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(Pooling x) { }

    /**
     *  Visit an LRN component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(LRN x) { }

    /**
     *  Visit a NonLinear component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(NonLinear x) { }

     /**
     *  Visit an Upsample component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(Upsample x) { }

    /**
     *  Visit a Transpose component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(Transpose x) {}


    /**
     *  Visit a GenericNeuron component.
     * @param  x A Visitor Object.
    */
    public void visitComponent(GenericNeuron x) { }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                  ///
    /**
     *  Increment the indentation.
     */
    protected void _prefixInc() {
        _prefix += _offset;
    }

    /**
     *  Decrement the indentation.
     */
    protected void _prefixDec() {
        if (_prefix.length() >= _offset.length()) {
            _prefix = _prefix.substring(_offset.length());
        }
    }

}
