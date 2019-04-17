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
 * @author Cintia Muranaka
 * @author Felipe de Carvalho Pereira [felipe.pereira@students.ic.unicamp.br]
 * @author Matheus Diógenes Andrade
 */
public class Main {

    public static int timeLimit = 30; // Max time in minutes
    public static int iterationsLimit = -1; // Max iterations (negative values for not using iterations limit)
    public static int valueLimit; // Value limit to stop iterations
    public static int size; // Number of variables
    public static String outputCsv; // Output name file

    // Instances
    public static final String[] FILES_LIST = new String[]{
        "instances/qbf020",
    	//"instances/qbf040",
    	//"instances/qbf060",
        //"instances/qbf080",
       // "instances/qbf100",
        //"instances/qbf200",
       // "instances/qbf400"
    };

    //Calls execution method with 5 different configurations
    public static void main(String[] args) throws IOException {

        outputCsv = "fileName,config,valueSol,tempExec\n";
        
        // Configurations
        executeTabuSearch(0.20, TS_QBFPT.STANDARD, TS_QBFPT.FIRST_IMPROVEMENT, "P");
        executeTabuSearch(0.20, TS_QBFPT.STANDARD, TS_QBFPT.BEST_IMPROVEMENT, "A");
        executeTabuSearch(0.10, TS_QBFPT.STANDARD, TS_QBFPT.FIRST_IMPROVEMENT, "B");
        executeTabuSearch(0.20, TS_QBFPT.PROBABILISTIC, TS_QBFPT.FIRST_IMPROVEMENT, "C");
        executeTabuSearch(0.20, TS_QBFPT.DIVERSIFICATION_RESTART, TS_QBFPT.FIRST_IMPROVEMENT, "D");
        
        saveOutput("output.csv", outputCsv); // Setting the name of the output file
    }
    
    private static void executeTabuSearch(double tenurePercent, int tabuStrategie, int localSearchStrategy, String configuration) throws IOException
    {
    	long beginTotalTime = System.currentTimeMillis();
    	
    	// Iterating over files
        for (String file : FILES_LIST) {
        	if(file.equals("instances/qbf020")) {
        		valueLimit = -125;
        		size = 20;
        	} else if(file.equals("instances/qbf040")) {
        		valueLimit = -366;
        		size = 40;
        	} else if(file.equals("instances/qbf060")) {
        		valueLimit = -576;
        		size = 60;
        	} else if(file.equals("instances/qbf080")) {
        		valueLimit = -1000;
        		size = 80;
    		} else if(file.equals("instances/qbf100")) {
        		valueLimit = -1539;
        		size = 100;
    		} else if(file.equals("instances/qbf200")) {
        		valueLimit = -5826;
        		size = 200;
    		}else if(file.equals("instances/qbf400")) {
        		valueLimit = -16625;
        		size = 400;
    		}

            //Print configurations of the execution
            System.out.println("Executing Tabu Search for file: " + file);
            System.out.println("Configuration:");
            printTenure((int)(tenurePercent*size));
            printTabuStrategie (tabuStrategie);
            printLocalSearchStrategy(localSearchStrategy);
            printStopCriterion();

            // Executing Tabu Search
            System.out.println("Execution:");

            long beginInstanceTime = System.currentTimeMillis();
            
            // Setting configurations parameters
            // tenure is defined by the ternurePercent * size
            TS_QBFPT ts = new TS_QBFPT((int)(tenurePercent*size), timeLimit, iterationsLimit, file,  tabuStrategie, localSearchStrategy, valueLimit);
            Solution<Integer> bestSolution = ts.solve(); // Starting solve model
            
            System.out.println(" maxVal = " + bestSolution); // Print best solution
            
            // Print other data
            long endInstanceTime = System.currentTimeMillis();
            long totalInstanceTime = endInstanceTime - beginInstanceTime;
            System.out.println("Time = " + (double) totalInstanceTime / (double) 1000 + " seg");
            System.out.println("\n");
            
            // Add info to output csv file
            outputCsv += file + "," + configuration + ","
                     + bestSolution.cost + "," + (double)totalInstanceTime / 1000 + "\n";

        }

        // Calculating time of all executions
        long totalTime = System.currentTimeMillis() - beginTotalTime;

        System.out.println("Execution time for all files: " + (totalTime / 1000D) + "seg \n"
                + "----------------------------------------------------- \n \n");
    }

    	
    // Print tabu strategy
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

    // Print tenure value
    private static void printTenure(int tenure) {
    	System.out.println(" Tenure = " + tenure);
    }

    // Print local search strategy
    private static void printLocalSearchStrategy(int localSearchStrategy) {
        String resp = " Local Search = ";

        if (localSearchStrategy == TS_QBFPT.FIRST_IMPROVEMENT) {
            resp += "First Improving";
        } else {
            resp += "Best Improving";
        }

        System.out.println(resp);
    }

    // Print stop criterion
    private static void printStopCriterion() {
    	
        String resp = " Stop Criterion = ";
        
        if(iterationsLimit <= 0)
        	resp += timeLimit + " minutes";
        
        if (iterationsLimit > 0) {
            resp +=  iterationsLimit + " iterations";
        }

        System.out.println(resp);
    }

    // Save input file
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
