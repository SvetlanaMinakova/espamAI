package espam.operations.codegeneration.sesame;

import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.codegeneration.sesame.cpp.CPPSDFGVisitor;
import espam.operations.codegeneration.sesame.h.HSDFGVisitor;
import espam.operations.codegeneration.sesame.ymlSDF.YmlSDFGVisitor;
import espam.utils.fileworker.FileWorker;
import java.util.Iterator;

/**
 * Class implements generation Sesame templates for an arbitrary SDF graph
 */
public class SesameSDFGVisitor {


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

         /** generate .yml config file and Makefile*/
         _ymlVisitor.callVisitor(y, templatesDir);
         System.out.println("Sesame application generated in: " + templatesDir);
     }

        catch (Exception e){
         System.err.println(templatesDir + "Sesame application generation error: " + e.getMessage());

        }
     }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ///

    /** header-files visitor*/
    public static HSDFGVisitor _hvisitor = new HSDFGVisitor();

    /**C++ code-files   visitor*/
    public static CPPSDFGVisitor _cppVisitor = new CPPSDFGVisitor();

    /** application yml and Makefile visitor*/
    public static YmlSDFGVisitor _ymlVisitor = new YmlSDFGVisitor();

}
