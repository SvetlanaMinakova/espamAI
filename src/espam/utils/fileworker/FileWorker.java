package espam.utils.fileworker;
import espam.datamodel.EspamException;

import java.io.*;
import java.util.Vector;

/**
 * Class, provides read and write operations for work with models represented as files
 */
public class FileWorker {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     *Get all the abs paths to files in the specified directory
     * @param dir directory with files
     * @return abs path to all the files in the specified directory
     */
      public  static Vector<String> getAllFilePaths(String dir){

          Vector<String> filePaths = new Vector<>();

          File myFolder = new File(dir);
          File[] files = myFolder.listFiles();
          for(File f: files){
              filePaths.add(f.getAbsolutePath());
          }

          return filePaths;

      }

        /**
     *Get all the abs paths to files with specified extension
     * in the specified directory
     * @param dir directory with files
     * @param extension files extension (filter)
     * @return abs path to all the files in the specified directory
     */
      public  static Vector<String> getAllFilePaths(String dir, String extension){

          Vector<String> filePaths = new Vector<>();

          File myFolder = new File(dir);
          File[] files = myFolder.listFiles();
          for(File f: files){
              String absPath = f.getAbsolutePath();
              if(absPath.endsWith(extension))
                filePaths.add(absPath);
          }

          return filePaths;

      }

    /**
     * Reads model from file
     * @param modelName name of the model
     * @return model description, if model was found and null otherwise
     */
    public static String read(String dir, String modelName, String extension) {
        String path = createPath(dir,modelName,extension);
        try { return read(path); }
        catch (Exception e) {
            System.err.println(modelName+" reading error." +e.getMessage());
        }
        return null;
    }

    /**
     * Writes string inside of the file. If file exists,
     * current file content is erased and replaced by
     * content parameter
     * @param dir directory to write files to/read files from
     * @param fileName name of the file
     * @param extension file extension
     * @param content content to be written into the file
     */
     public static void write(String dir, String fileName, String extension, String content) {
         String path = createPath(dir,fileName,extension);
        try {
            File directory = new File(dir);
            if( !directory.exists() ) {
                if( !directory.mkdirs() ) {
                    throw new EspamException("could not create directory '" + directory.getPath() + "'.");
                }
            }
            write(path,content,false);
        }
        catch (Exception e) {
            System.err.println(fileName+" writing error." +e.getMessage());
        }
    }

    /**
     * Writes string inside of the file. If file exists,
     * new strings are appended to current file content,
     * Otherwise a new file is created and filled up with content
     * @param dir directory to write files to/read files from
     * @param fileName name of the file
     * @param extension file extension
     * @param content content to be written into the file
     */
     public static void writeAppend(String dir, String fileName, String extension, String content) {
         String path = createPath(dir,fileName,extension);
        try {
            File directory = new File(dir);
            if( !directory.exists() ) {
                if( !directory.mkdirs() ) {
                    throw new EspamException("could not create directory '" + directory.getPath() + "'.");
                }
            }

            write(path,content,true);
        }
        catch (Exception e) {
            System.err.println(fileName+" writing error." +e.getMessage());
        }
    }

     /**
     * Creates empty file
     * @param dir directory to write files to/read files from
     * @param fileName name of the file
     * @param extension file extension
     */
     public static File createFile(String dir, String fileName, String extension) throws Exception {
         String path = createPath(dir, fileName, extension);
         String emptyString = "";
         File directory = new File(dir);
            if( !directory.exists() ) {
                if( !directory.mkdirs() ) {
                    throw new Exception("could not create directory '" + directory.getPath() + "'.");
                }
            }
         File file = new File(path);
         file.createNewFile();
         return file;
        }

    /**
     * @param dir directory contains file
     * @return stream of file
     * @throws FileNotFoundException
     */
    public static PrintStream openFile(String dir,String filename, String extension) throws Exception{


        File directory = new File(dir);
            if( !directory.exists() ) {
                if( !directory.mkdirs() ) {
                    throw new EspamException("could not create directory '" + directory.getPath() + ".");
                }
            }

        String path = createPath(dir,filename,extension);
        PrintStream printStream = null;

        try {
                OutputStream file = null;
                file = new FileOutputStream(path);
                printStream = new PrintStream(file);
            }
        catch(Exception e ) {
            System.out.println("Open file exception: " + e.getMessage());
        }
        return printStream;
    }

    /**
     * delete file if it exists
     * @param dir file directory (abs path)
     * @param fileName file name
     * @param extension file extension
     * @return true, if file was sucessfully deleted and false otherwise
     */
    public static boolean delete(String dir, String fileName, String extension){
       String path = createPath(dir,fileName,extension);
       File file = new File(path);
        if(file.delete()){
            return true;
        }else return false;
    }

     /**
     * delete directory and all files inside it
     * @param dir file directory (abs path)
     * @return true, if file was sucessfully deleted and false otherwise
     */
    public static void recursiveDelete(String dir) {

        File file = new File(dir);

        /** while not deleted */
        if (!file.exists())
            return;

        /** recursively dlete folder's content*/
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f.getAbsolutePath());
            }
        }
        /** for empty directories*/
        file.delete();
    }

    /**
     * Reads data from json-file
     * @param path path to json file
     */
    public static String read(String path) throws FileNotFoundException {
        File file = new File(path);
        if(!file.exists())
            throw new FileNotFoundException();

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader( file.getAbsoluteFile()));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                    ///
    /**
     * Writes data to file
     * @param path path to file
     * @param content content to be written into the file
     */
    private static void write(String path, String content, boolean append) {
        File file = new File(path);

        try {
            if(!file.exists()){
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            if(append)
               out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));

            try {
                out.print(content);
            } finally {
                out.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                    ///
    /**
     * Creates path to file from folder and file name
     * @param dir directory, contains model
     * @param fileName name of the file
     * @param extension file extension
     * @return path to json file
     */
    protected static String createPath(String dir, String fileName, String extension) {
        String path;
        if (extension==null)
          path = dir + File.separator + fileName;
        else
            path = dir + File.separator + fileName + "." + extension;

        return path;
    }
}
