package espam.visitor.json.refinement;

import espam.operations.refinement.CSDFGEnergyRefiner;
import espam.parser.json.JSONParser;
import espam.utils.fileworker.FileWorker;

import java.io.PrintStream;

/**
 * Visitor of the energy refiner
 */
public class EnergyRefinerVisitor {

    /**
     * print current exec times configuration in .json format
     */
    public static void printDefaultSpec(String dir){
        try {
            PrintStream printStream = FileWorker.openFile(dir,"energy_spec","json");
            printEnergyRefiner(printStream,CSDFGEnergyRefiner.getInstance());
            System.out.println(dir + "energy_spec.json file generated");
            printStream.close();
        }
        catch(Exception e){
            System.err.println("energy_spec printout error: " + e.getMessage());
        }
    }

    /**
     * Print energy refiner parameters in .json format
     * @param printStream printstream
     * @param refiner energy refiner
     */
   private static void printEnergyRefiner(PrintStream printStream,CSDFGEnergyRefiner refiner ){
       StringBuilder offset = new StringBuilder("  ");
       printStream.println("{");
       printStream.println( "  \"alpha\": " + refiner.getAlpha() + ",");
       printStream.println( "  \"beta\": " + refiner.getBeta() + ",");
       printStream.println( "  \"b\": " + refiner.getB());
       printStream.println("}");
   }


}
