package espam.operations.codegeneration.erqian.h;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.operations.codegeneration.sesame.h.HSDFGVisitor;
import espam.utils.fileworker.FileWorker;

public class HSDFGVisitorErqian extends HSDFGVisitor{

        /**
     * generate main class template contains R/W primnitives description
     * and other common stuff.
     */
    public void generateMainClassTemplate(String dir){
        try {
            _printStream = FileWorker.openFile(dir, _mainClassName, "h");
            _writeMainClassHBeginning(_mainClassName,_mainClassName);
            _writeMainClassFunctions();
            _writeCommonEnd(_mainClassName);
            _printStream.close();
        }
        catch (Exception e){
            System.err.println(".cpp file creation error for " + _mainClassName + " " + e.getMessage());
        }
    }

       /**
     * Call .h visitor
     * @param y CSDF graph node
     * @param dir target directory for templates
     */
      @Override
     public void callVisitor(CSDFNode y, String dir){
         try {
             _printStream = FileWorker.openFile(dir, y.getName(), "h");
             _writeCommonBeginning(y.getName(),_getBaseClassName(y));
             _writeFIFOsizes(y);
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
     * Begin a header file with common beginning
     * @param  className name of the corresponding C++ class
     */
    @Override
    public void _writeCommonBeginning(String className, String baseClassName ) {

         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        _printStream.println("");
        _printStream.println("class " + name + "{");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");
        _printStream.println(_prefix + "void main();");
        /** specific const parameters definition*/
        _printStream.println(_prefix + "void* memobj_cpu;");

        _prefixDec();
    }

    public void _writeMainClassHBeginning(String className, String baseClassName ) {

         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        _printStream.println("");
        _printStream.println("class " + name + "{");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");
        _printStream.println(_prefix + "void main();");
        _prefixDec();
    }

    /**
     * Write application main class .cpp beginning
     */
    protected void _writeMainClassFunctions(){
        _printStream.println("// Main function");
        _prefixInc();
        _printStream.println(_prefix + "void " + _mainClassName + "::main();");
        _prefixDec();
        _printStream.println();
        _printStream.println(_prefix + "// Read/Write primitives");
        prefixInc();
        _writeRWPrimitives();
        prefixDec();
        _printStream.println("");
        _printStream.println(_prefix + "// Execution function primitive");
        prefixInc();
        _writeExecPrimitive();
        prefixDec();
        _printStream.println("");
        _printStream.println(_prefix + "/**");
        _printStream.println(_prefix + "Data shift function (for shifting overlapping data in I/O arrays)");
        _printStream.println(_prefix + "@param array : I/O overlapping array");
        _printStream.println(_prefix + "@param dim   : I/O overlapping array dimensionality");
        _printStream.println(_prefix + "*/");
        prefixInc();
        _writeShiftFunction();
        prefixDec();
    }

    /**
     * Write shift function (for shifting overlapping data in I/O arrays)"
     */
    protected void _writeShiftFunction(){
        _printStream.println(_prefix + "void " + _mainClassName + "::data_shift(void *array, int dim);");
    }

    /**
     * Write r/w primitive functions templates
     */
    protected void _writeRWPrimitives(){
        for (int dim=1;dim <=_maxPrimitiveDimensionality; dim++){
         _printStream.println(_prefix +"// " + dim + "D");
         _printStream.println(_prefix + "void " + _mainClassName +
                    "::read" + _primitivePostfix + "_"+ dim + "D " +
                    "(void* fifo, void* memobj_cpu, int len, int fifo_size);");
         _printStream.println(_prefix + "void " + _mainClassName +
                    "::write" + _primitivePostfix + "_"+ dim + "D " +
                    "(void* fifo, void* memobj_cpu, int len, int fifo_size);");
        }
    }

      /**
     * Write execution function primitive
     */
    protected void _writeExecPrimitive(){
        _printStream.println(_prefix + "void " + _mainClassName +
                    "::execute (std::string function);");
    }

    /**
     * Write FIFO sizes for CSDF node
     * @param node CSDF node
     */
    protected void _writeFIFOsizes(CSDFNode node){
        _printStream.println("");
        _printStream.println(_prefix + "//FIFO sizes");
        prefixInc();
        for(CSDFPort inport: node.getInPorts())
            _writeFIFOsize(inport);
        for(CSDFPort outport: node.getOutPorts())
            _writeFIFOsize(outport);
        prefixDec();
        _printStream.println("");
    }

     /**
     * Write FIFO sizes for CSDF node
     * @param port CSDF port
     */
     protected void _writeFIFOsize(CSDFPort port){
      MemoryUnit mu = port.getAssignedMemory();
      if(mu==null)
          return;
      if(mu.getDimensionality()<1)
          return;
      _printStream.println(_prefix + "const int " + port.getName() + "_fifo_size = "
              + mu.getShape().getElementsNumber() + ";");

    }

    ///////////////////////////////////////////////////////////////////
    ///                private variables                           ///

    /** primitive postfix*/
    private static String _primitivePostfix = "SWF_CPU";

    /** application main class name*/
    private static String _mainClassName = "appMain";

    /** max r/w primitives dimensionality */
    private static int _maxPrimitiveDimensionality = 3;


}
