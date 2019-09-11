package espam.visitor.sesame.h;

import espam.datamodel.graph.cnn.operators.ComplexOperator;
import espam.datamodel.graph.cnn.operators.InternalBuffer;
import espam.datamodel.graph.cnn.operators.Operator;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;
import java.util.*;

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
             _writeCommonEnd(y.getName());

         }
         catch (Exception e){
             System.err.println(".h file creation error for " + y.getName() + ". " + e.getMessage());
         }
     }

     /** TODO: make common for DNN-CSDF and normal CSDF
     * Write container templates for each container, associated with the node
     * @param node SDF graph node
     */
    protected void _writeDNNRefinedContainerTemplates(CSDFNode node){
        for(CSDFPort inport: node.getNonOverlapHandlingInPorts()){
            MemoryUnit mu = inport.getAssignedMemory();
            if (mu!=null)
                _writeEmptyLinearArr(mu.getName(), mu.getTypeDesc(), mu.getShape().getElementsNumber());

        }

        /** define only distinct out ports*/
        Vector<String> defined = new Vector<>();
        for(CSDFPort outport: node.getNonOverlapHandlingOutPorts()){
                MemoryUnit mu = outport.getAssignedMemory();
                if (mu!=null) {
                    if(!defined.contains(mu.getName())) {
                         _writeEmptyLinearArr(mu.getName(), mu.getTypeDesc(), mu.getShape().getElementsNumber());
                        defined.add(mu.getName());
                    }
                }
        }
        defined.clear();
    _printStream.println(_prefix + "// specific node parameters definition");
    _defineOperatorParameters(node.getOperator());

    }

     /**
     * Define operator parameters
     * @param operator CSDF node operator
     */
    protected void _defineOperatorParameters(Operator operator){
        _defineOperatorParameters(operator,null);
    }

    /**
     * Define operator parameters
     * @param operator CSDF node operator
     */
    protected void _defineOperatorParameters(Operator operator, Integer suboperatorId){

    if(operator == null)
    return;

    if(operator instanceof ComplexOperator){
        _defineComplexOperatorParameters((ComplexOperator) operator);
        return;
    }

    String parPrefix = "";
    if(suboperatorId!=null)
        parPrefix = "_" + suboperatorId.toString();

    _printStream.println();
    _printStream.println(_prefix + "std::map<std::string,int> int_params" + parPrefix + ";");
    _printStream.println(_prefix + "std::map<std::string," + _paramdataType + " *> tensor_params" + parPrefix + ";");

    _printStream.println("//tensor parameters " + parPrefix);
        TreeMap<String,Tensor> tensorParams = operator.getTensorParams();
        for(Map.Entry<String,Tensor> tensorParam : tensorParams.entrySet())
            _writeArrLinear(tensorParam.getValue(),tensorParam.getKey() + parPrefix,_paramdataType);

        _printStream.println("//const int parameters " + parPrefix);
        TreeMap<String,Integer> intParams = operator.getIntParams();
            for (Map.Entry<String,Integer> intPar: intParams.entrySet()) {
                _printStream.println(_prefix + "const int " + intPar.getKey() + parPrefix + " = " + intPar.getValue() + ";");
        }
    }

    /**
     * Define operator parameters for a complex operator
     * @param operator complex CSDF node operator
     */
    protected void _defineComplexOperatorParameters(ComplexOperator operator){
        Integer opId = 0;
        for(Operator subOp: operator.getSubOperators()){
            _defineOperatorParameters(subOp,opId);
            opId++;
        }

        _printStream.println();
        _printStream.println(_prefix + "// internal buffers");
        Integer bufId = 0;
        for(InternalBuffer internalBuffer: operator.getInternalBuffers()){
            _defineInternalBuffer(internalBuffer,bufId);
            bufId++;
        }
    }

    /**
     * Define internal buffer between two connections
     * @param internalBuffer internal buffer of the CSDF node
     */
    protected void _defineInternalBuffer (InternalBuffer internalBuffer, Integer Id){
       Tensor buffer = new Tensor( internalBuffer.getBufferSize());
       String name = "internal_buf" + Id.toString();
       _writeArrLinear(buffer,name,_IOdataType);
    }

    /**
     * Write empty linear array
     * @param name name of the array
     * @param typeDesc description of array type;
     */
    private void _writeEmptyLinearArr(String name, String typeDesc, int size){
        if(size<1)
            return;

        _printStream.println(_prefix + typeDesc + " " + name + "[" + size + "] = {0}; ");
    }

    /**
     * Define linear array
     * @param tensor shape of array (espam. Tensor)
     * @param arrname name of the array
     * @param typeDesc description of array type;
     */
     public void _writeArrLinear(Tensor tensor, String arrname, String typeDesc){
        if(Tensor.isNullOrEmpty(tensor))
             return;

        int size = tensor.getElementsNumber();
        _writeEmptyLinearArr(arrname,typeDesc,size);
        /** TODO: refactoring!*/
        if(!arrname.equals("input"))
        _printStream.println(_prefix + "const int " + arrname + "_len = " + size + ";");
     }

    /**
     * Wtite sizes of additional tensor params
     * @param additionalArrNames additional array names
     * @param node CSDF node
     */
     private void _writeAdditionalArrLens(Vector<String> additionalArrNames, CSDFNode node){
         for (String arrName: additionalArrNames){
             MemoryUnit additionalArr = node.getMemoryUnit(arrName);
             _writeArrLen(additionalArr);
         }

     }

     /**
     * Define size of a tensor memory unit
     * @param mu a tensor memory unit
     */
     private void _writeArrLen(MemoryUnit mu){
        if (mu==null)
            return;
        Tensor muShape = mu.getShape();
        if(muShape==null)
            return;
        int size =muShape.getElementsNumber();
        _printStream.println(_prefix + "const int " + mu.getName() + "_len = " + size + ";");
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
    protected void _writeContainerTemplate(CSDFPort port) {
        MemoryUnit mu = port.getAssignedMemory();
        if (mu == null) {
            Tensor defaultMemoryShape = new Tensor(_findMinMemSize(port));
            mu = new MemoryUnit(port.getName(), defaultMemoryShape,MemoryUnit.getDefaultDataType());
        }
        _writeTensorToCPPArrayDefinition(mu.getShape(), mu.getName(), mu.getTypeDesc());
    }

    /**
     * Find min memory size could be associated with a port
     * @param port CSDFPort
     * @return min memory size could be associated with a port
     */
    protected int _findMinMemSize(CSDFPort port){
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
     * corresponding to  espam. With Tensor dimensions, descibed as const ints
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
        for (int i = tensorDimensionality-1; i>=0; i--)
            _printStream.println(_prefix + "const int " + name + "_dim_" + (tensorDimensionality-i-1) +" = " +tensor.getDimSize(i)+";");

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
     * Get C++ definition of static multidimensional array,
     * corresponding to  espam.
     * @param tensor espam. Tensor
     * @param name name of the array
     * @param typeDesc description of array type;
     * @return C++ description of static multidimensional array,
     * corresponding to  espam. Tensor
     */
    public void _writeTensorToCPPArrayDefinitionNoSizes(Tensor tensor, String name, String typeDesc){
        if(Tensor.isNullOrEmpty(tensor))
            return;

        _printStream.println(_prefix + "//"+ name +" array definition");
        _prefixInc();
        int tensorDimensionality = tensor.getDimensionality();

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
     * Get C++ definition of static multidimensional array,
     * corresponding to  espam. Tensor
     * @param tensor espam. Tensor
     * @param name name of the array
     * @param typeDesc description of array type;
     * @return C++ description of static multidimensional array,
     * corresponding to  espam. Tensor
     * TODO do smth with tensor reverse!!
     */
    public void _writeTensorToCPPArrayDefinitionWeights(Tensor tensor, String name, String typeDesc){
        if(Tensor.isNullOrEmpty(tensor))
            return;

        _printStream.println(_prefix + "//"+ name +" array definition");
        _prefixInc();
        int tensorDimensionality = tensor.getDimensionality();
       // int revId = tensorDimensionality-1;
        for (int i =0;i< tensorDimensionality; i++)
            _printStream.println(_prefix + "const int " + name + "_dim_" + i +" = " +tensor.getDimSize(i)+";");

        _printStream.println(_prefix + "const int " + name + "_len = " +tensor.getElementsNumber()+";");

        StringBuilder defsb = new StringBuilder(typeDesc);
        defsb.append(" ");
        defsb.append(name);

        for (int i =0;i< tensorDimensionality; i++)
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
        _printStream.println("#include <map>");
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

    public String getIOdataType() {
        return _IOdataType;
    }

    public void setIOdataType(String _IOdataType) {
        HSDFGVisitor._IOdataType = _IOdataType;
    }

    public static void setParamdataType(String _paramdataType) {
        HSDFGVisitor._paramdataType = _paramdataType;
    }

    public static String getParamdataType() {
        return _paramdataType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ///
    /**flag shows, if the HDNN visitor is refined for CNNs*/
    protected boolean _CNNRefined = false;

    /** if weights should be initialized with external text files*/
    private static boolean _loadWeights = true;

    /** folder, from where weights should be loaded*/
    private static String _weightsFolder = "./";

    /** I/O data type*/
    private static String _IOdataType = "float";

    /** parameters data type*/
    private static String _paramdataType = "float";
}
