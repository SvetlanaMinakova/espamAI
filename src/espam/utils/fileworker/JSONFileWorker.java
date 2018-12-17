package espam.utils.fileworker;

public class JSONFileWorker extends FileWorker {

///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Reads model from .json -file
     * @param dir directory with the model
     * @param modelName name of the model
     * @return description of model in DOT-format, if model was found and null otherwise
     */
    public static String read(String dir, String modelName) {
       return read(dir,modelName,"json");
    }

    /**
     * Creates .json file for model and writes model into it
     * @param modelName name of the model
     */
    /**
     *
     * @param dir directory to write the model
     * @param modelName name of the model
     * @param model String model description
     */
     public static void write(String dir, String modelName, String model) {
         write(dir ,modelName,"json",model);
    }

}
