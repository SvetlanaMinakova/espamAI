
//recursive copy directory
package espam.visitor.xps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copier {
    
    private File fromFile;
    private File toFile;
    private boolean recursive;
    
    private long totalBytes = 0;
    private long currentBytes = 0;
    private long countCopiedFiles = 0;
    
    private int typeDestination;
    
    private boolean isFinished = false;
    
    static public final int DESTINATION_IS_DIRECTORY = 1;
    static public final int DESTINATION_IS_FILE = 2;
    
    /**
     * Constructor for Copier.
     */
    public Copier(File from, File to, int destination) {
        this(from, to, destination, false);
    }
    
    public Copier(File from, File to, int destination, boolean recursive) {
        super();
        
        this.fromFile = from;
        this.toFile = to;
        this.typeDestination = destination;
        this.recursive = recursive;
        
        // getTotalBytes(fromFile);
    }
    
    public Copier(String from, String to, int destination) {
        this(new File(from), new File(to), destination, false);
    }
    
    public Copier(String from, String to, int destination, boolean recursive) {
        this(new File(from), new File(to), destination, recursive);
    }
    
    private void initCopy() {
        if (fromFile == null || toFile == null)
            throw new IllegalArgumentException("Parameters must not be null");
        
        if (!fromFile.exists())
            throw new IllegalArgumentException("From does not exist: " + fromFile.getPath());
        
        if (fromFile.isDirectory() && toFile.exists() && !toFile.isDirectory()) //isDirectory() geeft false terug, indien to niet bestaat
            throw new IllegalArgumentException("When From is a directory, To also has to be one: " + toFile.getPath());
        
        if (!fromFile.isDirectory() && toFile.isDirectory())
            toFile = new File(mergeFileName(toFile.getPath(), fromFile.getName()));
        
        currentBytes = 0;
    }
    
    /*
     private void getTotalBytes(File from) {
     if (from.isDirectory()) {
     File[] files = from.listFiles();
     for (int i=files.length - 1; i >= 0; i--) {
     if (!files[i].isDirectory() || recursive)
     getTotalBytes(files);
     }
     }
     else
     totalBytes += from.length();
     }
     */
    
    public File getFrom() {
        return fromFile;
    }
    
    
    public void setFrom(File from) {
        this.fromFile = from;
        // getTotalBytes(fromFile);
    }
    
    public File getTo() {
        return toFile;
    }
    
    public void setTo(File to, int destination) {
        this.toFile = to;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    /*
     public long getTotalBytes() {
     return totalBytes;
     }
     */
    
    public int getPercent() {
        if (totalBytes == 0)
            return 0;
        
        return (int) ((double) currentBytes / totalBytes * 100.);
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public long copy() throws IOException, FileNotFoundException {
        initCopy();
        _copy(fromFile, toFile, typeDestination);
        isFinished = true;
        return countCopiedFiles;
    }
    
    private String  mergeFileName(String path, String name) {
        return path + "/" + name;
    }
    
    
    private void _copy(File from, File to, int destination) throws IOException, FileNotFoundException {
        if (from.isDirectory()) {
            if (!to.exists()) {
                if (!to.mkdirs())
                    throw new IOException("The following directory could not be created: " + to.getPath());
            }
            File[] files = from.listFiles();
            for (int i=files.length - 1; i >= 0; i--) {
                if (!files[i].isDirectory())
                    _copy(files[i], to, DESTINATION_IS_DIRECTORY);
                else if (recursive) {
                    _copy(files[i], new File(mergeFileName(to.getPath(), files[i].getName())), DESTINATION_IS_FILE);
                }
            }
        }
        else {
            if (destination == DESTINATION_IS_DIRECTORY) {
                if (!to.exists()) {
                    if (!to.mkdirs())
                        throw new IOException("The following directory could not be created: " + to.getPath());
                }
                copyFile(from, new File(mergeFileName(to.getPath(), from.getName())));
            }
            else
                copyFile(from, to);
        }
    }
    
    private void copyFile(File from, File to) throws IOException, FileNotFoundException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        int size;
        
        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            bos = new BufferedOutputStream(new FileOutputStream(to));
            byte[] buf = new byte[512];
            
            while ((size = bis.read(buf)) > -1) {
                bos.write(buf, 0, size);
                currentBytes += size;
            }
            
            if (bis != null)
                bis.close();
            bis = null;
            
            if (bos != null)
                bos.close();
            bos = null;
            
            countCopiedFiles++;
        }
        finally {
            if (bis != null)
                bis.close();
            bis = null;
            
            if (bos != null)
                bos.close();
            bos = null;
        }
    }
    
    static public long copy(File from, File to, int destination) throws IOException, FileNotFoundException {
        return copy(from, to, destination, false);
    }
    
    static public long copy(File from, File to, int destination, boolean recursive) throws IOException, FileNotFoundException {
        return new Copier(from, to, destination, recursive).copy();
    }
    
    public static void main(String[] args) {
        try {
            File f = new File("./a");
            File t = new File("./b");
            Copier.copy(f, t, 1, true);
        } catch (Exception e) {
            System.out.println("err:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
