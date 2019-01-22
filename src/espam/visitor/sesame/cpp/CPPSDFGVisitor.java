package espam.visitor.sesame.cpp;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.IndexPair;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.util.Iterator;
import java.util.Vector;

/**
 * Class implements generation of C++ code files (.cpp) for CSDF graph
 */
public class CPPSDFGVisitor extends CSDFGraphVisitor{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

     /**
     * Call CPP SDFG Visitor
     * @param y  corresponding CSDFNode
     * @param dir directory for .cpp templates
     */
     public void callVisitor(CSDFNode y, String dir){
         try {
             _printStream = FileWorker.openFile(dir, y.getName(), "cpp");
             _writeCommonCppBeginning(y.getName());
             _writeAdditionalLibraries();
             _writeCppConstructorAndDestructor(y.getName(), _getBaseClassName(y));
             _writeMain(y);
         }
         catch (Exception e){
             System.err.println(".cpp file creation error for node" + y.getName() + " " + e.getMessage());
         }
     }

     ///////////////////////////////////////////////////////////////////
    ////                         protected methods                    ///

    /**
     * Get CSDFNode base .cpp class. If SDF Node have group, the base class is a group class.
     * Otherwise, base class is node own class
     * @param node CSDFNode
     * @return CSDFNode base .cpp class
     */
    protected String _getBaseClassName(CSDFNode node){

     //   String baseClass = node.getGroup();

     //   if(baseClass==null || baseClass=="")

        String  baseClass = node.getName()+"_Base";
       return baseClass;
    }

    /**
     * Write main function for the node
     * @param y SDF Node
     */
    protected void _writeMain(CSDFNode y) {
        _printStream.println("void " + y.getName() + "::main() {");
        _prefixInc();
        _printStream.println(_prefix + "// repetition parameters definition");
        _printStream.println(_prefix + "int q = " + y.getRepetitions() + ";");
        _printStream.println(_prefix + "int phase_len = " + y.getLength() + ";");
        _printStream.println(_prefix + "int phase; ");
         _printStream.println(" ");
        _printStream.println(_prefix + "// while (1) {");
        _prefixInc();
        _printStream.println(_prefix + "// loop over the repetitions number");
        _printStream.println(_prefix + "for (int rep = 0; rep < q ; rep ++) {");
        _prefixInc();
        _printStream.println(_prefix + "phase = rep % phase_len;");
        _processReading(y);
        _processExecution(y);
        _processWriting(y);
        _prefixDec();
        _printStream.println(_prefix + "}// loop over the phases");
        _prefixDec();
        _printStream.println(_prefix + "//} while (1)");
        _prefixDec();
        _printStream.println(_prefix + "} // main");
    }

    /**
     * process node input ports
     * @param node SDF Node
     */
    protected void _processReading(CSDFNode node){
       _printStream.println("");
       _printStream.println(_prefix + "//reading");
        for (CSDFPort inport: node.getInPorts()){
            _definePhasesLimitation(inport, false);
            printReadTemplate(inport);
        }
    }

    /**
     * process exec function with default name
     * @param node CSDF node
     */
    protected void _processExecution(CSDFNode node){
        _processExecution(node,"execute");
    }

     /**
      * process execution phase.
      * Execution describes processing of input data
      * by elementary operations. The general format is
      * for(each kernel in kernels)
      *     for(repetition on data chunk)
      *         run elementary operation;
      *
      * Where number of kernels (operation instances, processing the same data
      * with different context) = number of neurons of DNN block
      * and =1 by default
      *
      * @param node SDF Node
     *  @param execPrimitiveName name of the execution primitive
     */
    protected void _processExecution(CSDFNode node,String execPrimitiveName){
      int opRepetitionsNum = node.getOperationRepetitionsNumber();
      if(opRepetitionsNum==0)
          return;
      String operation = node.getOperation();
      if(operation==null)
          return;

      _printStream.println("");
      _printStream.println(_prefix + "//execution");
      int kernels = node.getKernelsNum();

      if(kernels>1){
         prefixInc();
         _printStream.println(_prefix + "for (int n = 0; n < " + kernels + "; n++) {");
      }

      if(opRepetitionsNum>1){
          prefixInc();
          _printStream.println(_prefix + "for (int i = 0; i < " + opRepetitionsNum + "; i++) {");
      }

      _printStream.println(_prefix + execPrimitiveName +"(\"" + operation + "\");");

      if(opRepetitionsNum>1) {
          prefixDec();
          _printStream.println(_prefix + "}");
      }

      if(kernels>1){
         prefixDec();
         _printStream.println(_prefix + "}");
      }

    }

      /**
     * process node output ports
     * @param node SDF Node */

    protected void _processWriting(CSDFNode node){
       _printStream.println("");
       _printStream.println(_prefix + "//writing");
        for (CSDFPort outport: node.getOutPorts()){
           _definePhasesLimitation(outport, false);
            printWriteTemplate(outport);
        }
    }

    /**
     * Define tokens reading/writing boundaries for SDF port/ SDF Port group
     * @param port SDF Port
     * @param isGroupLeader flag, shows if port defines limitations for a group
     */
    protected void _definePhasesLimitation(CSDFPort port, boolean isGroupLeader) {
         String portName = port.getName();
        if(isGroupLeader)
            _printStream.println(_prefix + "// port group leader: " + portName);

        _processStartLimitations(port.getStartTokens(),portName);
        _processEndLimitations(port.getRates(),portName);
    }

    /**
     * Print max tokens num to be processed by the port
     * @param rates port rates
     * @param portName port name
     */
    protected void _processEndLimitations(Vector<IndexPair> rates, String portName){
        int phasesNum = rates.size();
        if (phasesNum == 0) {
            _printStream.println(_prefix + "// phases definition error: no phases defined");
            return;
        }

        int firstPhaseRate = rates.get(0).getFirst();
        _printStream.println(_prefix + "//max tokens port " + portName);
        _printStream.println(_prefix + "int " + portName + "_tokens = " + firstPhaseRate + ";");

        int prevPhaseId = rates.get(0).getSecond();
        int nextPhaseId = rates.get(0).getSecond();

        int rate;
        for (int i = 1; i< phasesNum; i++) {
            IndexPair phase = rates.get(i);
            nextPhaseId += phase.getSecond();
            rate = phase.getFirst();
            _printStream.println(_prefix + "if (phase >= " + prevPhaseId + ")");
            prefixInc();
            _printStream.println(_prefix + portName + "_tokens = " + rate + "; ");
            _prefixDec();
            prevPhaseId = nextPhaseId;
        }
    }

     /**
     * Print Shifts : for overlapping processing
     * @param shifts rates, describing shift from the target array while reading/writing
     * @param portName port name
     */
    protected void _processStartLimitations(Vector<IndexPair> shifts, String portName){
        if(shifts==null)
            return;

        int phasesNum = shifts.size();
        if (phasesNum == 0)
            return;

        int firstPhaseRate = shifts.get(0).getFirst();
        _printStream.println(_prefix + "// shift " + portName);
        _printStream.println(_prefix + "int " + portName + "_shift = " + firstPhaseRate + ";");

        int prevPhaseId = shifts.get(0).getSecond();
        int nextPhaseId = shifts.get(0).getSecond();
        int rate;
        for (int i = 1; i<shifts.size(); i++) {
            IndexPair phase = shifts.get(i);
            nextPhaseId += phase.getSecond();
            rate = phase.getFirst();
            _printStream.println(_prefix + "if (phase >= " + prevPhaseId + ")");
            prefixInc();
            _printStream.println(_prefix + portName + "_shift = " + rate + "; ");
            _prefixDec();
            prevPhaseId = nextPhaseId;
        }

    }


    /**
     * Write common beginning for all generated nodes, contains:
     *  - definition of header
     *  - definition of standard libraries
     *  - definition of namespace
     * @param className name of the .cpp class
     */
    protected void _writeCommonCppBeginning(String className){
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
         /** TODO: should I define any libraries in here?? Or they will
          * TODO be copied from the graphName.so file?*/
        _printStream.println("#include \""+ className + ".h\"");
        _printStream.println("#include <stdlib.h>");
        _printStream.println("#include <iostream>");
        _printStream.println("using namespace std;");
        _printStream.println("");
    }

    /** TODO any additional libraries definition?*/
    protected void _writeAdditionalLibraries(){ }

    /**
     * Write constructor and destructor .cpp definitions
     * @param className name of the .cpp class
     */
    protected void  _writeCppConstructorAndDestructor(String className, String baseClassName){
        _printStream.println(className + "::" + className + "(Id n, " + className + "_Ports *ports) : " + baseClassName + "(n, ports) {}");
        _printStream.println(className + "::~" + className + "() {}");
        _printStream.println("");
    }

    /**
     * Print read  template
     * @param port CSDF port performs reading
     */
    public void printReadTemplate(CSDFPort port) {
        String arrayName = port.getAssignedMemoryName();
        if(port.getStartTokens()==null) {
            printOperationTemplate(port, "read",arrayName);
            return;
        }
        String shiftDesc = port.getName() + "_shift";
        printOperationShiftedTemplate(port,"read",arrayName,shiftDesc);
    }

  //  public void printReadTemplate(Vector<CSDFPort> portsGroup) {
    //    printOperationTemplate(portsGroup,groupName,"read",dataDimensionality,arrayName);
  //  }

    /**
     * Print write template
     * @param port CSDF port performs writing
     */
    public void printWriteTemplate(CSDFPort port) {
        String arrayName = port.getAssignedMemoryName();
        printOperationTemplate(port,"write",arrayName);
    }

    /**
     * print reading/writing template for port,
     * taking into account only end border limitations
     */
    public void printOperationTemplate(CSDFPort port,String operation, String arrayName){
       if(port==null || arrayName==null)
           return;
        int dataDimensionality = port.getMemoryDim();

        _printStream.println(" ");
        _printStream.println(_prefix + "// " + operation + " to " + arrayName);
        String nestedIndex = getNestedIndex(dataDimensionality,arrayName);
        String indexedInputName = arrayName + nestedIndex;
        _printStream.println(_prefix + " for ( int t = 0; t < " + port.getName() + "_tokens; t++) {");
         prefixInc();
        _printStream.println(_prefix + "ports->" + port.getName() + "." + operation + "(" + indexedInputName + ");");
        _prefixDec();
        _printStream.println(_prefix + "}");
    }

    /**
     * print reading/writing template for port,
     * taking into account only end border limitations
     */
    public void printOperationTemplate(CSDFPort groupLeader,String operation, String arrayName, Vector<String> ports){
       if(groupLeader==null || arrayName==null)
           return;
        int dataDimensionality = groupLeader.getMemoryDim();

        _printStream.println(" ");
        _printStream.println(_prefix + "// " + operation + " to " + arrayName);
        String nestedIndex = getNestedIndex(dataDimensionality,arrayName);
        String indexedInputName = arrayName + nestedIndex;
        _printStream.println(_prefix + " for ( int t = 0; t < " + groupLeader.getName() + "_tokens; t++) {");
         prefixInc();
         for(String port: ports) {
             _printStream.println(_prefix + "ports->" + port + "." + operation + "(" + indexedInputName + ");");
         }
        _prefixDec();
        _printStream.println(_prefix + "}");
    }


       /**
     * print reading/writing template for port,
     * taking into account only end border limitations
     */
    public void printOperationShiftedTemplate(CSDFPort port,String operation, String arrayName, String shiftDesc){
       if(port==null || arrayName==null)
           return;
        int dataDimensionality = port.getMemoryDim();
        _printStream.println(" ");
        _printStream.println(_prefix + "// " + operation + " to " + arrayName);
        String nestedIndex = getNestedIndex(dataDimensionality,arrayName);
        String indexedInputName = arrayName + nestedIndex;
        _printStream.println(_prefix + " for ( int t = " + shiftDesc + "; t < (" + port.getName() + "_tokens + "
                + shiftDesc + "); t++) {");
        prefixInc();
        _printStream.println(_prefix + "ports->" + port.getName() + "." + operation + "(" + indexedInputName + ");");
        prefixDec();
        _printStream.println(_prefix + "}");
    }

     /**
     * print conditions, changing the index
     * TODO t is supposed to be a variable, running over the tokens number
     * TODO do smth with tensor reverse!!
     */
    public String getNestedIndex(int dataDimensionality, String arrayName) {
        if (dataDimensionality == 1)
            return "[t]";

        StringBuilder result = new StringBuilder();
        /** construct the rest of dim ids*/
        for (int i = dataDimensionality-1; i>0; i--) {

            /**construct next dimension*/
            result.append("[");

            /** add brackets on % operation - for all dimensions,
             *  except of the last dimension
             */
            if (i != dataDimensionality - 1)
                result.append("(");

            result.append("t/(");
            /** create nested index*/
            for (int j = 0; j < i; j++) {
                result.append(arrayName + "_dim_" + j);
                if (j != i - 1)
                    result.append("*");
            }
            result.append(")");
            /** add % operation to provide starting with 0 for next id increment*/
            if (i != dataDimensionality - 1) {
                result.append(")%" + arrayName + "_dim_" + i);
            }

            result.append("]");
        }

        /**construct first dim id*/
        result.append("[t%" + arrayName + "_dim_0]");

        return result.toString();
    }
}
