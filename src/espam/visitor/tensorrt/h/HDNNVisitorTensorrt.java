package espam.visitor.tensorrt.h;
import espam.datamodel.graph.cnn.Network;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CNNGraphVisitor;

public class HDNNVisitorTensorrt extends CNNGraphVisitor {

    /**
     * Call .h visitor
     * @param y CNN Layer
     * @param dir target directory for templates
     */
     public void callDNNVisitor(Network y, String dir){
         try {
             _printStream = FileWorker.openFile(dir, y.getName(), "h");
             _writeCommonBeginning(y.getName(),"gpu_partition");
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
    public void _writeCommonBeginning(String className, String baseClassName ) {

         String name = className;
        _printStream.println("// File automatically generated by ESPAM");
        _printStream.println("");
        _printStream.println("#ifndef " + name + "_H");
        _printStream.println("#define " + name + "_H");
        _printStream.println("");
        //tensorrt classes
        _printStream.println("#include \"NvInfer.h\"");
        _printStream.println("#include \"common.h\"");
        _printStream.println("#include \"cuda_runtime_api.h\"");

        _printStream.println("#include <map>");
        _printStream.println("#include <vector>");
        _printStream.println("#include <thread>");


        _printStream.println("#include \""+ baseClassName + ".h\"");
        _printStream.println("");
        _printStream.println(_prefix + "using namespace std;");
        _printStream.println(_prefix + "using namespace nvinfer1;");
        _printStream.println("");
        _printStream.println("class " + name + " : public " + baseClassName + " {");
        _printStream.println("public:");
        _prefixInc();
        _printStream.println(_prefix + name + "();");
        _printStream.println(_prefix + "virtual ~" + name + "();");
        _printStream.println("");

        _printStream.println(_prefix + "//DNN-dependent functions");
        _printStream.println(_prefix + "ICudaEngine* createEngine(std::map<std::string, Weights>& weightMap, unsigned int maxBatchSize, IBuilder* builder, nvinfer1::DataType dt) override;// DNN");
        _printStream.println(_prefix + "void generate_dummy_weights() override; //generate dummy weights");
        _printStream.println(_prefix + "void init_params() override; //initializer of DNN-specific parameters");

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


}