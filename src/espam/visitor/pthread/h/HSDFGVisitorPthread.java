package espam.visitor.pthread.h;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.datamodel.graph.csdf.CSDFPort;
import espam.datamodel.graph.csdf.datasctructures.MemoryUnit;
import espam.datamodel.graph.csdf.datasctructures.Tensor;
import espam.visitor.sesame.h.HSDFGVisitor;
import espam.utils.fileworker.FileWorker;

import java.util.Vector;

public class HSDFGVisitorPthread extends HSDFGVisitor{

     /**
     * generate main class template contains application control logic
     */
    public void generateMainClassTemplate(String dir){
        try {
            _printStream = FileWorker.openFile(dir, _mainClassName, "h");
            _writeMainClassHBeginning(_mainClassName);
            _writeCommonEnd(_mainClassName);
            _printStream.close();
        }
        catch (Exception e){
            System.err.println(".cpp file creation error for " + _mainClassName + " " + e.getMessage());
        }
    }

         /**
     * generate main class template contains R/W primnitives description
     * and other common stuff.
     */
    public void generateFuncClassTemplate(String dir){
        try {
            _printStream = FileWorker.openFile(dir, _funcClassName, "h");
            _writeFuncClassHBeginning(_funcClassName);
            _writeFunctions();
            _writeCommonEnd(_funcClassName);
            _printStream.close();
        }
        catch (Exception e){
            System.err.println(".cpp file creation error for " + _funcClassName + " " + e.getMessage());
        }
    }

     /**
     * Call .h visitor of base class template
     * @param dir target directory for templates
     */
     public void generateBaseClassTemplate(String dir){
         String className = "csdfNode";
         try {
             _printStream = FileWorker.openFile(dir, className, "h");
              String name = className;
             _printStream.println("// File automatically generated by ESPAM");
             _printStream.println("");_printStream.println("#ifndef " + name + "_H");
             _printStream.println("#define " + name + "_H");
             _printStream.println("#include <string>");
             _printStream.println("");
             _printStream.println("class " + name + "{");
             _printStream.println("public:");
             _prefixInc();
             _printStream.println(_prefix + name + "();");
             _printStream.println(_prefix + "virtual ~" + name + "();");
             _printStream.println(_prefix + "//abstract main function");
             prefixInc();
             _printStream.println(_prefix + "virtual void main(void *threadarg) = 0;");
             prefixDec();

             _writeCommonEnd(className);

         }
         catch (Exception e){
             System.err.println(".h file creation error for " + className + ". " + e.getMessage());
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

             _printStream.println(_prefix + "const int neurons = " + y.getKernelsNum() + ";");
             _writeFIFOsizes(y);

             //TODO REMOVE AFTER TESTING
             if(y.getName().equals("input")) {
                _writeTensorToCPPArrayDefinition(new Tensor(32,32),"data","int");
            }

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
        _printStream.println("#include \"" + _baseClassName + ".h\"");
        _printStream.println("#include <map>");
        _printStream.println("");
        _printStream.println("class " + name +" : public " + _baseClassName +  "{");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");
        _printStream.println(_prefix + "void main(void *threadarg) override;");
        _printStream.println(_prefix + "// specific const parameters definition");
        _printStream.println(_prefix + "std::map<std::string,const int*>int_params;");

        _prefixDec();
    }

    public void _writeMainClassHBeginning(String className) {

         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        // Include specific headers
        _printStream.println("#include <stdlib.h>");
        _printStream.println("#include <iostream>");
        _printStream.println("#include <vector>");
        _printStream.println("#include \"" + _baseClassName + ".h\"");
        _printStream.println("#include \""+_funcClassName+".h\"");
        _printStream.println("#include \"types.h\"");
        _printStream.println("");
        _printStream.println("class " + name + " {");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");
        _printStream.println("// list of available nodes");
        _printStream.println(_prefix + "static std::map< std::string,csdfNode* > nodes;");
        _printStream.println("");
        _printStream.println(_prefix + "void main();");
        _printStream.println();
        _printStream.println("// Call of the specific node ");
        _printStream.println(_prefix + "static void run_node(std::string node_name);");
        _prefixDec();
    }

    public void _writeFuncClassHBeginning(String className) {
         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        // Include specific headers
        _printStream.println("#include <stdlib.h>");
        _printStream.println("#include <iostream>");
        _printStream.println("#include <string>");
        _printStream.println("#include <vector>");
        _printStream.println("#include \"types.h\"");
        _printStream.println("");
        _printStream.println("class " + name + " {");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _prefixDec();
    }

    /**
     * Define application main class functions
     */
    protected void _writeFunctions(){
        _printStream.println("");
        _printStream.println(_prefix + "// Execution function primitive");
        prefixInc();
        _writeExecPrimitive();
        _writeExecPrimitive("int");
        prefixDec();
        _printStream.println("");
        _printStream.println(_prefix + "/**");
        _printStream.println(_prefix + "Data shift function (for shifting overlapping data in I/O arrays)");
        _printStream.println(_prefix + "@param array : I/O overlapping array");
        _printStream.println(_prefix + "@param dim   : I/O overlapping array dimensionality");
        _printStream.println(_prefix + "*/");
        prefixInc();
        _writeCPULine("int");
        _writeCPULine("float");
        _printStream.println();
        _writeShiftFunctions("int");
        _writeShiftFunctions("float");
        _printStream.println("");
        _writePrintFunctions("int");
        _writePrintFunctions("float");
        _writeGetBufFunc("src");
        _writeGetBufFunc("dst");
        prefixDec();
    }

    /**
     * Print function of getting buffer from vector of buffers
     */
    protected void _writeGetBufFunc(String bufPrefix){
     _printStream.println(_prefix + "static fifo_buf* get_buf_by_" + bufPrefix +
     " (std::string name, std::vector<fifo_buf>& fifos);");
    }


    /**
     * Write line copy function
     * TODO replace by data loader in real implementation
     * @param dataType type of line copy function
     */
    protected void _writeCPULine(String dataType){
         _printStream.println(_prefix + "// line copy function, type: " + dataType);
         _printStream.println(_prefix + "static void cpy_2D_data_line(const int &data_w, "+
                 dataType + " *src,"+ dataType + " *dst,"+" const int &line_id);");
    }


    /**
     * Write shift function (for shifting overlapping data in I/O arrays)"
     * TODO refactoring: shift(tensor)
     */
    protected void _writeShiftFunctions(String dataType){
        _printStream.println(_prefix + "// matrix shift functions, type: " + dataType);
        _printStream.println(_prefix + "static void shift_2D(const int &h, const int &w, "+ dataType + " *x, const int &stride);");
        _printStream.println(_prefix + "static void shift_3D(const int &d, const int &h, const int &w, "+ dataType + " *x, const int &stride);");
    }

    /**
     * Write matrix print functions
     * TODO refactoring: print(matrix)
     */
    protected void _writePrintFunctions(String dataType){
        _printStream.println(_prefix + "// matrix print functions, type: " + dataType);
        _printStream.println(_prefix + "static void print_2D (const int &h, const int &w, "+ dataType + " *x);");
        _printStream.println(_prefix + "static void print_3D (const int &d, const int &h, const int &w, "+ dataType + " *x);");
    }


    /**
     * Write r/w primitive functions templates
     */
    protected void _writeRWPrimitives(){
        for (int dim=1;dim <=_maxPrimitiveDimensionality; dim++){
         _printStream.println(_prefix +"// " + dim + "D");
          /** external r/w*/
            _writeMocRWPrimitive("read" + _externalRWPostfix ,dim);
            _writeMocRWPrimitive("write"+ _externalRWPostfix ,dim);
        }
    }

    /**
     * Write moc of R/W primitive
     * @param primitiveName name of the primitive: read or write
     * @param dim primitive dimensionality
     */
    protected void _writeMocRWPrimitive(String primitiveName, int dim){
        _printStream.println(_prefix + "static void " +
                    primitiveName + "_"+ dim + "D " +
                    "(void* fifo, void* memobj_cpu, int len, int fifo_size);");

    }

      /**
     * Write execution function primitive
     */
    protected void _writeExecPrimitive(){
        _printStream.println(_prefix + "static void execute (std::string function);");
    }

    /**
     * Write execution function primitive
     */
    protected void _writeExecPrimitive(String tensorParamType){
        _printStream.println(_prefix + "static void execute (std::string function," +
                tensorParamType +"* input, " + tensorParamType + "* weights, "
                + tensorParamType + "* output, std::map<std::string,const int*>* int_params_ptr );");
    }

    /**
     * Write FIFO sizes for CSDF node
     * @param node CSDF node
     */
    protected void _writeFIFOsizes(CSDFNode node){
        _printStream.println("");
        _printStream.println(_prefix + "//FIFO sizes");
        prefixInc();
        for(CSDFPort inport: node.getNonOverlapHandlingInPorts())
             _printStream.println(_prefix + "int " + inport.getName() + "_fifo_size;");
        for(CSDFPort outport: node.getNonOverlapHandlingOutPorts())
             _printStream.println(_prefix + "int " + outport.getName() + "_fifo_size;");
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
     * TODO check tensor order!!

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

        for (int i = 0; i<tensorDimensionality; i++)
            defsb.append("[" + tensor.getDimSize(i) + "]");

          /** static array definition
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
    } */

    ///////////////////////////////////////////////////////////////////
    ///                private variables                           ///

    /** primitive postfix*/
    private static String _externalRWPostfix = "SWF_CPU";

    /** primitive postfix*/
   // private static String _internalRWPostfix = "_Internal";

    /** application main class name*/
    private static String _mainClassName = "appMain";

    /** max r/w primitives dimensionality */
    private static int _maxPrimitiveDimensionality = 3;

    /** CSDF graph node base class*/
    private static String _baseClassName = "csdfNode";

    /** CSDF graph node functions class*/
    private static String _funcClassName = "appFunc";

}
