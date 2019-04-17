package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import problems.qbf.solvers.TS_QBFPT;
import solutions.Solution;

/**
 * Class that executes the Tabu Search for MAXQBFPT problem
 *
 * Standard configuration:
 * Tenure = 20
 * Tabu strategie = standard
 * Local search = first improvement
 *
 * @author Felipe de Carvalho Pereira [felipe.pereira@students.ic.unicamp.br]
 */
public class Main {

    public static int timeLimit = 30; // In minutes
    public static int iterationsLimit = 1000; // Iterations without improvement in incumbent (negative values for not using iterations limit)
    public static int iterationsToDiversify = 10000;
    public static String outputCsv;

    public static final String[] FILES_LIST = new String[]{
        "instances/qbf020",
    	"instances/qbf040",
    	"instances/qbf060",
        "instances/qbf080",
        "instances/qbf100",
        "instances/qbf200",
        //"instances/qbf400"
    };

    //Calls execution method with 5 different configurations
    public static void main(String[] args) throws IOException {

        outputCsv = "fileName,tenure,tabuStrategie,localSearchStrategie,valueSol\n";

        executeTabuSearch(20, TS_QBFPT.STANDARD, TS_QBFPT.FIRST_IMPROVEMENT);
        //executeTabuSearch(20, TS_QBFPT_FELIPE.STANDARD, TS_QBFPT_FELIPE.BEST_IMPROVEMENT);
        //executeTabuSearch(10, TS_QBFPT_FELIPE.STANDARD, TS_QBFPT_FELIPE.FIRST_IMPROVEMENT);
        //executeTabuSearch(20, TS_QBFPT_FELIPE.PROBABILISTIC, TS_QBFPT_FELIPE.FIRST_IMPROVEMENT);
        //executeTabuSearch(20, TS_QBFPT_FELIPE.DIVERSIFICATION_RESTART, TS_QBFPT_FELIPE.FIRST_IMPROVEMENT);
        
        saveOutput("output.csv", outputCsv);
    }
    
    private static void executeTabuSearch(int tenure, int tabuStrategie, int localSearchStrategie) throws IOException
    {
    	long beginTotalTime = System.currentTimeMillis();
    	
    	// Iterating over files
        for (String arquivo : FILES_LIST) {

            //Print configurations of the execution
            System.out.println("Executing Tabu Search for file: " + arquivo);
            System.out.println("Configuration:");
            printTenure(tenure);
            printTabuStrategie (tabuStrategie);
            printLocalSearchStrategie(localSearchStrategie);
            printStopCriterion();

            // Executing Tabu Search
            System.out.println("Execution:");

            long beginInstanceTime = System.currentTimeMillis();
            
            TS_QBFPT ts = new TS_QBFPT(tenure, timeLimit, iterationsLimit, arquivo,  tabuStrategie, localSearchStrategie, iterationsToDiversify);
            Solution<Integer> bestSolution = ts.solve();
            System.out.println(" maxVal = " + bestSolution);
            
            long endInstanceTime = System.currentTimeMillis();
            long totalInstanceTime = endInstanceTime - beginInstanceTime;
            System.out.println("Time = " + (double) totalInstanceTime / (double) 1000 + " seg");
            System.out.println("\n");
            
            outputCsv += arquivo + "," + tenure + "," + tabuStrategie + "," + localSearchStrategie + ","
                     + bestSolution.cost + "\n";

        }

        // Calculating time of all executions
        long totalTime = System.currentTimeMillis() - beginTotalTime;

        System.out.println("Execution time for all files: " + (totalTime / 1000D) + "seg \n"
                + "----------------------------------------------------- \n \n");
    }

    private static void printTabuStrategie(int tabuStrategie) {
        String resp = " Tabu strategie = ";

        if (tabuStrategie == TS_QBFPT.STANDARD) {
            resp += "Standard";
        }
        if (tabuStrategie == TS_QBFPT.PROBABILISTIC) {
            resp += "Probabilistic";
        }
        if (tabuStrategie == TS_QBFPT.DIVERSIFICATION_RESTART) {
            resp += "Diversification restart";
        }

        System.out.println(resp);
    }

    private static void printTenure(int tenure) {
    	System.out.println(" Tenure = " + tenure);
    }

    private static void printLocalSearchStrategie(int localSearchStrategie) {
        String resp = " Local Search = ";

        if (localSearchStrategie == TS_QBFPT.FIRST_IMPROVEMENT) {
            resp += "First Improving";
        } else {
            resp += "Best Improving";
        }

        System.out.println(resp);
    }

    private static void printStopCriterion() {
    	
        String resp = " Stop Criterion = ";
        
        if(iterationsLimit <= 0)
        	resp += timeLimit + " minutes";
        
        if (iterationsLimit > 0) {
            resp +=  iterationsLimit + " iterations";
        }

        System.out.println(resp);
    }

    public static void saveOutput(String fileName, String content) {
        File dir;
        PrintWriter out;

        dir = new File("output");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            out = new PrintWriter(new File(dir, fileName));
            out.print(content);
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
