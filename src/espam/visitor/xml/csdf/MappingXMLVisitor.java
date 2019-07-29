package espam.visitor.xml.csdf;

import espam.datamodel.mapping.MFifo;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;
import espam.datamodel.mapping.Mapping;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.utils.fileworker.FileWorker;
import espam.visitor.MappingVisitor;

import java.io.PrintStream;

public class MappingXMLVisitor extends MappingVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * Call CPP SDFG Visitor
     * @param dir directory for sesame templates
     *
     */
     public static void callVisitor(Mapping mapping, String dir) {
         try {
            PrintStream ps = FileWorker.openFile(dir, mapping.getName(), "xml");
            MappingVisitor mv = new MappingXMLVisitor(ps);
            mapping.accept(mv);
            ps.close();
            System.out.println("Mapping file generated in: " + dir + "/" + mapping.getName() + ".xml");
        }
        catch (Exception e){
            System.err.println("mapping file creation error: "+ e.getMessage());
        }

     }

     public MappingXMLVisitor(PrintStream ps){
         _printStream = ps;
     }


    /**
     *  Visit a Mapping component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(Mapping x) {
        _printStream.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        _printStream.println("<!DOCTYPE mapping PUBLIC \"-//LIACS//DTD ESPAM 1//EN\"");
        _printStream.println("\"http://www.liacs.nl/~cserc/dtd/espam_1.dtd\">");
        _printStream.println("<mapping name=\"" + x.getName() + "\">");

        for (Object procObj: x.getProcessorList()) {
                MProcessor proc = (MProcessor)procObj;
                visitComponent(proc);
        }

        _printStream.println("</mapping>");
    }

    /**
     *  Visit a MFifo component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(MFifo x) {
    }

    /**
     *  Visit a MProcess component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(MProcess x) {
    _printStream.println("  " + "<process name=\""+ x.getName() + "\" />");
    }

      /**
     *  Visit a MProcess component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDProcess x) {

    }

    /**
     *  Visit a MProcessor component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(MProcessor x) {
        _printStream.println(" " + "<processor name=\""+ x.getName() + "\">");
        for (Object procObj: x.getProcessList()) {
                MProcess proc = (MProcess) procObj;
                visitComponent(proc);
        }
        _printStream.println(" " + "</processor>");

    }


    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///
    private PrintStream _printStream;

}
