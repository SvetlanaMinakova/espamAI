package espam.operations.codegeneration.erqian;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.codegeneration.erqian.cpp.CPPSDFGVisitorErqian;
import espam.operations.codegeneration.erqian.h.HSDFGVisitorErqian;
import espam.utils.fileworker.FileWorker;

import java.util.Iterator;

public class ErqianSDFGVisitor {
        ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    /**
     * Call CPP SDFG Visitor
     * @param y  CSDFGraph
     * @param dir directory for sesame templates
     */
     public static void callVisitor(CSDFGraph y, String dir, boolean CNNRefined){
     String templatesDir = dir + "app/";

     try {
         /** if templates directory already exists,
          *  remove it and all templates inside it*/
         FileWorker.recursiveDelete(templatesDir);
         _hvisitor.setCNNRefined(CNNRefined);
         /** generate .cpp and .h files for each SDF graph node */
         Iterator i = y.getNodeList().iterator();
         CSDFNode node;
         while (i.hasNext()) {
             node = (CSDFNode) i.next();
             _hvisitor.callVisitor(node, templatesDir);
             _cppVisitor.callVisitor(node, templatesDir);
         }

         /** TODO generate  Makefile*/
        // _ymlVisitor.callVisitor(y, templatesDir);
         System.out.println("Sesame application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Sesame application generation error: " + e.getMessage());

        }
     }

     /** TODO makefile generation??*/

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /** header-files visitor*/
    public static HSDFGVisitorErqian _hvisitor = new HSDFGVisitorErqian();

    /**C++ code-files visitor*/
    public static CPPSDFGVisitorErqian _cppVisitor = new CPPSDFGVisitorErqian();

}
