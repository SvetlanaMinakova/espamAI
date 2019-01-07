package espam.operations.codegeneration.sesame.h;

import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class implements generation of C++ header files (.h) for an arbitrary CSDF graph
 */
public class HSDFGVisitor extends CSDFGraphVisitor {

     ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /** public constructor is called in HDNNVisitor*/
    /** TODO replace by singleton instance (which is safer)*/
    public HSDFGVisitor(){}
    public void setPrintStream(PrintStream printStream){
        _printStream = printStream;
    }

     /**
     * Call .h visitor
     * @param y CSDF graph node
     * @param dir target directory for templates
     */
     public void callVisitor(CSDFNode y, String dir){
         try {
             _printStream = FileWorker.openFile(dir, y.getName(), "h");
             _writeCommonBeginning(y.getName(),_getBaseClassName(y));
             _writeContainerTemplates(y);
             /** write specific data*/
             _printStream.println(_prefix + "//specific node parameters and functions");
             _writeCommonEnd(y.getName());

         }
         catch (Exception e){
             System.err.println(".h file creation error for " + y.getName() + ". " + e.getMessage());
         }
     }

      /**
     * Write container templates for each container, associated with the node
     * @param node SDF graph node
     */
    private void _writeDNNRefinedContainerTemplates(CSDFNode node){
        for(CSDFPort inport: node.getInPorts()){
            if(!inport.isOverlapHandler()) {
                MemoryUnit mu = inport.getAssignedMemory();
                if (mu!=null) {
                    _writeTensorToCPPArrayDefinition(mu.getShape(),mu.getName(),mu.getTypeDesc());
                }
            }
        }

        /** define only distinct out ports*/
        Vector<String> defined = new Vector<>();
        for(CSDFPort outport: node.getOutPorts()){
            if(!outport.isOverlapHandler()) {
                MemoryUnit mu = outport.getAssignedMemory();
                if (mu!=null) {
                    if(!defined.contains(mu.getName())) {
                         _writeTensorToCPPArrayDefinition(mu.getShape(), mu.getName(), mu.getTypeDesc());
                        defined.add(mu.getName());
                    }
                }
            }
        }
        defined.clear();
        /** define weights, if any*/
        MemoryUnit weights = node.getMemoryUnit("weights");
        if(weights!=null){
           _writeTensorToCPPArrayDefinition(weights.getShape(), weights.getName(), weights.getTypeDesc());
        }
        /** define constant parameters, if any*/
        Vector<MemoryUnit> constParams = node.getUnitParams();
        if(constParams.size()>0) {
            _printStream.println("//const parameters");
            for (MemoryUnit mu : constParams) {
                _printStream.println(_prefix + "const " + mu.getTypeDesc() + " " + mu.getName() + " = " + mu.getUnitParamDesc() + ";");
            }
        }
    }

     /**
     * Write container templates for each container, associated with the node
     * @param node SDF graph node
     */
    public void _writeContainerTemplates(CSDFNode node){
        if(_CNNRefined) {
            _writeDNNRefinedContainerTemplates(node);
            return;
        }

        /** generate .cpp and .h files for each SDF graph node */
         Iterator i = node.getPortList().iterator();
         CSDFPort port;
         while (i.hasNext()) {
          port = (CSDFPort) i.next();
          _writeContainerTemplate(port);
        }
    }

  /**
     * Write container templates for each port
     * If specific memory unit is assigned for the port, this specific memory is described.
     * Otherwise, a default container is generated. Default container is a linear container, stores integer units.
     * @param port CSDFPort
     */
    private void _writeContainerTemplate(CSDFPort port) {
        MemoryUnit mu = port.getAssignedMemory();
        if (mu == null) {
            Tensor defaultMemoryShape = new Tensor(_findMinMemSize(port));
            mu = new MemoryUnit(port.getName(), defaultMemoryShape,"int");
        }
        _writeTensorToCPPArrayDefinition(mu.getShape(), mu.getName(), mu.getTypeDesc());
    }

    /**
     * Find min memory size could be associated with a port
     * @param port CSDFPort
     * @return min memory size could be associated with a port
     */
    private int _findMinMemSize(CSDFPort port){
        int minSize = 0;
        Vector<IndexPair> rates = port.getRates();
        if(rates==null)
            return 0;

        for(IndexPair rate: rates){
            if(rate.getFirst()>minSize)
                minSize=rate.getFirst();
        }
        return minSize;
    }


     /**
     * "Hardcode" C++ definition of static multidimensional array,
     * corresponding to DNN node parameter, defined in espam. Tensor format
     * @param shape DNN node  shape, defined in espam. Tensor format
     * @param name name of the parameter
     * @param typeDesc description of tensor value type;
     * @return C++ description of static multidimensional array,
     * corresponding to  espam. Tensor
      * TODO add actual values, or reference to actual values src
     */
    public void _writeTensorToConstCPPArray(Tensor shape,String name, String typeDesc){
        if(Tensor.isNullOrEmpty(shape))
            return;
        _printStream.println(" ");
       // _printStream.println(_prefix + "//"+ name +" constant array definition");

        String def = typeDesc + " " + name;
        for (int dim: shape.getShape())
            def += "[" + dim +"]";

         def += " = ";

        for (int i = 0 ;i < shape.getDimensionality(); i++)
             def += "{";

        def += "1";

        for (int i = 0 ;i < shape.getDimensionality(); i++)
             def += "}";
        def += ";";

        _printStream.println(_prefix + def);
        _printStream.println("");
    }

       /**
     * Get CSDFNode base .cpp class. If SDF Node have group, the base class is a group class.
     * Otherwise, base class is node own class
     * @param node CSDFNode
     * @return CSDFNode base .cpp class
     */
    public String _getBaseClassName(CSDFNode node){

     //   String baseClass = node.getGroup();

     //   if(baseClass==null || baseClass=="")

        String  baseClass = node.getName()+"_Base";
       return baseClass;
    }

    /**
     * Get C++ definition of static multidimensional array,
     * corresponding to  espam. Tensor
     * @param tensor espam. Tensor
     * @param name name of the array
     * @param typeDesc description of array type;
     * @return C++ description of static multidimensional array,
     * corresponding to  espam. Tensor
     * TODO do smth with tensor reverse!!
     */
    public void _writeTensorToCPPArrayDefinition(Tensor tensor, String name, String typeDesc){
        if(Tensor.isNullOrEmpty(tensor))
            return;

        _printStream.println(_prefix + "//"+ name +" array definition");
        _prefixInc();
        int tensorDimensionality = tensor.getDimensionality();
       // int revId = tensorDimensionality-1;
        for (int i = 0; i< tensorDimensionality; i++)
            _printStream.println(_prefix + "const int " + name + "_dim_" + i +" = " +tensor.getDimSize(i)+";");

        StringBuilder defsb = new StringBuilder(typeDesc);
        defsb.append(" ");
        defsb.append(name);

        for (int i = tensorDimensionality-1; i>=0; i--)
            defsb.append("[" + tensor.getDimSize(i) + "]");

          /** static array definition*/
        defsb.append(" = ");

        for (int i = 0 ;i < tensorDimensionality; i++)
             defsb.append("{");

        defsb.append("0");

        for (int i = 0 ;i < tensorDimensionality; i++)
             defsb.append("}");

        defsb.append(";");

        _printStream.println(_prefix + defsb.toString());
        prefixDec();
        _printStream.println("");
    }


     /**
     * Begin a header file with common beginning
     * @param  className name of the corresponding C++ class
     */
    public void _writeCommonBeginning(String className, String baseClassName ) {

         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        _printStream.println("");
        _printStream.println("#include \""+ baseClassName + ".h\"");
        _printStream.println("");
        _printStream.println("class " + name + " : public " + baseClassName + " {");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "(Id n, " + name + "_Ports *ports);");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");
        _printStream.println(_prefix + "void main();");
        _prefixDec();
    }

    /**
     * Finish a header file with common ending
     * @param  className name of the corresponding C++ class
     */
    public void _writeCommonEnd(String className) {
        _printStream.println("};");
        _printStream.println("#endif // " + className + "_H");
    }

    /**
     * Check if the visitor is refined by DNN model
     * @return true, if the visitor is refined by DNN model and false otherwise
     */
    public boolean isCNNRefined() { return _CNNRefined; }

    /**
     * Set flag, shows if the container is CNN-refined
     * @param CNNRefined flag, shows if the container is CNN-refined
     */
    public void setCNNRefined(boolean CNNRefined) {
        this._CNNRefined = CNNRefined;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ///
    /**flag shows, if the HDNN visitor is refined for CNNs*/
    protected boolean _CNNRefined = false;
}
