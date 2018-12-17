package espam.visitor.xml.csdf;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.utils.fileworker.FileWorker;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;

/**
 * SDF model xml visitor, operating over the
 * Graph visitor
 * Graph properties visitor
 * */
public class CSDFXMLVisitor {


    public static void callVisitor(CSDFGraph graph, String dir, boolean oneToOneDummyMapping){
        try {
            PrintStream printStream = FileWorker.openFile(dir, graph.getName(), "xml");
            CSDFGraphVisitor xmlGraphVisitor = new CSDFGraphXMLVisitor(printStream);
        }

        catch (Exception e){
            System.err.println("XML CSDFGraph visitor call error."+ e.getMessage());
        }

    }

        public static void callVisitorWithDummyMapping(CSDFGraph graph, String dir, boolean oneToOneDummyMapping){
        try {
            PrintStream printStream = FileWorker.openFile(dir, graph.getName(), "xml");
            CSDFGraphVisitor xmlGraphVisitor = new CSDFGraphXMLVisitor(printStream);
        }

        catch (Exception e){
            System.err.println("XML CSDFGraph visitor call error."+ e.getMessage());
        }
    }



}
