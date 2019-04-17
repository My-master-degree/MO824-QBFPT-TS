package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import solutions.Solution;
import triple.Triple;
import triple.TripleElement;

/**
 * This class extends from TS_QBF and implements the characteristics of QBFPT problem
 * It  also implements two tabu search strategies in addition to the standard strategy:
 * the diversification by restart and probabilistic tabu
 * Besides the best improvement strategy for local search, it also implements
 * the first improvement strategy
 * 
 * @author Cintia Muranaka
 * @author Felipe de Carvalho Pereira [felipe.pereira@students.ic.unicamp.br]
 * @author Matheus Diógenes Andrade
 */
public class TS_QBFPT extends TS_QBF {
	
	/**
     * List of element objects used in prohibited triples. These objects
     * represents the variables of the model.
     */
    private TripleElement[] tripleElements;

    /**
     * List of prohibited triples.
     */
    private Triple[] triples; 
    
    // Tabu strategies
    public static final int STANDARD = 1;
    public static final int PROBABILISTIC  = 2;
    public static final int DIVERSIFICATION_RESTART = 3;
    private final int tabuStrategie;
    private final double timeToDiversify; // Diversification occurs every timeToDiversify minutes
    
    // Local Search strategies
    public static final int FIRST_IMPROVEMENT = 1;
    public static final int BEST_IMPROVEMENT = 2;
    private final int localSearchStrategie;
    
    private final int timeLimit; // Execution limit by time
    private final int valueLimit; // Execution limit by best solution value

	public TS_QBFPT(Integer tenure, int timeLimit, Integer iterations, String filename, int tabuStrategie, int localSearchStrategie, int valueLimit) throws IOException {
		super(tenure, iterations, filename);
		// TODO Auto-generated constructor stub
		
		this.tabuStrategie = tabuStrategie;
		this.localSearchStrategie = localSearchStrategie;
		this.timeToDiversify = (double)timeLimit / ObjFunction.getDomainSize();
		this.timeLimit = timeLimit;
		this.valueLimit = valueLimit;
		
		generateTripleElements();
        generateTriples();
	}
	
    /**
     * A Tabu CL generator for MAXQBFPT problem
     *
     * @return A list of candidates to be evaluated in neighborhood
     */
    @Override
    public ArrayList<Integer> makeCL() {
        int n = ObjFunction.getDomainSize();
        ArrayList<Integer> _CL = new ArrayList<Integer>(n);

        for (TripleElement tripElem : this.tripleElements) {
            tripElem.setAvailable(true);
            tripElem.setSelected(false);
            _CL.add(tripElem.getIndex());
        }

        return _CL;
    }
	
    /**
     * Update the list of candidates, keeping only those who
     * can be inserted in the incumbent solution without violating
     * any triple
     */
	@Override
    public void updateCL() {
        ArrayList<Integer> _CL = new ArrayList<Integer>();
        
        // Set all elements as available
        for (TripleElement tripElem : this.tripleElements) {
        	tripElem.setSelected(false);
        	tripElem.setAvailable(true);
        }
        
        // Set all incumbent elements as unavailable and selected
        if (this.incumbentSol != null) {
            for (Integer e : this.incumbentSol) {
                this.tripleElements[e].setSelected(true);
                this.tripleElements[e].setAvailable(false);
            }
        }
        
        // Set to unavailable those elements that can violate at least one triple
        for (Triple trip : this.triples) {
            TripleElement te0, te1, te2;
            te0 = trip.getElements()[0];
            te1 = trip.getElements()[1];
            te2 = trip.getElements()[2];

            if (te0.getSelected() && te1.getSelected()) {
                te2.setAvailable(false);
            } else if (te0.getSelected() && te2.getSelected()) {
                te1.setAvailable(false);
            } else if (te1.getSelected() && te2.getSelected()) {
                te0.setAvailable(false);
            }
        }

        // Update all CL with only available elements
        for (TripleElement tripElem : this.tripleElements) {
            if (!tripElem.getSelected() && tripElem.getAvailable()) {
                _CL.add(tripElem.getIndex());
            }
        }

        this.CL = _CL;
    }
	

    /**
     * Linear congruent function l used to generate pseudo-random numbers.
     */
    public int l(int pi1, int pi2, int u, int n) {
        return 1 + ((pi1 * u + pi2) % n);
    }

    /**
     * Function g used to generate pseudo-random numbers
     */
    public int g(int u, int n) {
        int pi1 = 131;
        int pi2 = 1031;
        int lU = l(pi1, pi2, u, n);

        if (lU != u) {
            return lU;
        } else {
            return 1 + (lU % n);
        }
    }

    /**
     * Function h used to generate pseudo-random numbers
     */
    public int h(int u, int n) {
        int pi1 = 193;
        int pi2 = 1093;
        int lU = l(pi1, pi2, u, n);
        int gU = g(u, n);

        if (lU != u && lU != gU) {
            return lU;
        } else if ((1 + (lU % n)) != u && (1 + (lU % n)) != gU) {
            return 1 + (lU % n);
        } else {
            return 1 + ((lU + 1) % n);
        }
    }

    /**
     * Method that generates a list of n prohibited triples using l g and h
     * functions
     */
    private void generateTriples() {
        int n = ObjFunction.getDomainSize();
        this.triples = new Triple[ObjFunction.getDomainSize()];

        for (int u = 1; u <= n; u++) {
            TripleElement te1, te2, te3;
            Triple newTriple;

            te1 = tripleElements[u - 1];
            te2 = tripleElements[g(u - 1, n) - 1];
            te3 = tripleElements[h(u - 1, n) - 1];
            newTriple = new Triple(te1, te2, te3);
            
            //Sorting new triple
            Arrays.sort(newTriple.getElements(), Comparator.comparing(TripleElement::getIndex));

            //newTriple.printTriple();
            this.triples[u-1] = newTriple;
        }
    }
    

    /**
     * That method generates a list of objects (Triple Elements) that represents
     * each binary variable that could be inserted into a prohibited triple
     */
	private void generateTripleElements() {
        int n = ObjFunction.getDomainSize();
        this.tripleElements = new TripleElement[n];

        for (int i = 0; i < n; i++) {
            tripleElements[i] = new TripleElement(i);
        }
    }
	
	/**
	 * This method make a local search based on neighborhood of the incumbent solution
	 * It makes a first or best improvement search depending on the execution settings
	 */
	@Override
	public Solution<Integer> neighborhoodMove() {
		ArrayList <ArrayList<Integer>> neighborhood = getNeighborhood(); // Get neighborhood of incumbent
		
		// Auxiliar variables
		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;
		minDeltaCost = Double.POSITIVE_INFINITY;
		Integer candIn, candOut;
		
		
		for(int i = 0; i < neighborhood.size(); i++)
		{
			candIn = neighborhood.get(i).get(0);
			candOut = neighborhood.get(i).get(1);
			Double deltaCost;
			if(candOut == fake) // Insertion
			{
				deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
				if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = null;
					}
				}
			}
			else if(candIn == fake) // Removal
			{
				deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
				if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = null;
						bestCandOut = candOut;
					}
				}
			}
			else // Exchange
			{
				deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
			if(this.localSearchStrategie == FIRST_IMPROVEMENT && deltaCost < 0) // Stop the search with the first improvement in incumbent
			{
				i = neighborhood.size();
			}
			
		}
		
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			incumbentSol.remove(bestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			incumbentSol.add(bestCandIn);
			CL.remove(bestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		
		ObjFunction.evaluate(incumbentSol);
		
		return null;
	}
	
	/**
	 * Generate a complete list with all the neighborhood of a incumbent, including all operations
	 * If first improvement local search is settled then it shuffles the list
	 * If the probabilistic strategy is settled then it shuffles the list and return half of it 
	 * 
	 * @return the neighborhood list
	 */
	private ArrayList <ArrayList<Integer>> getNeighborhood()
	{
		ArrayList <ArrayList<Integer>> neighborhood = new ArrayList<ArrayList<Integer>>();
		
		for(Integer i : CL) // It considers only the elements on candidate list
		{
			neighborhood.add(new ArrayList<Integer>(Arrays.asList(i, fake))); // Insertion
			
			for (Integer j : incumbentSol) {
				neighborhood.add(new ArrayList<Integer>(Arrays.asList(i, j))); // Exchange
            }
		}
		
		for (Integer j : incumbentSol) { 
			neighborhood.add(new ArrayList<Integer>(Arrays.asList(fake, j))); // Removal
        }
		
		if(this.tabuStrategie == PROBABILISTIC)
		{
			Collections.shuffle(neighborhood);
			int lastIndex = (int) (neighborhood.size() * 0.50) - 1;
			ArrayList <ArrayList<Integer>> probabilisticNeighborhood =  new ArrayList<ArrayList<Integer>>(neighborhood.subList(0, lastIndex));
			return probabilisticNeighborhood; // Return half of the list
		}
		
		if(this.localSearchStrategie == FIRST_IMPROVEMENT)
			Collections.shuffle(neighborhood); // Randomize the order
		
		return neighborhood;
	}
	
	public void diversify()
	{
		TripleElement[] sortedTripleElements = tripleElements.clone();
		Arrays.sort(sortedTripleElements, Comparator.comparing(TripleElement::getIncumbentFrequency)); // Sort elements by frequency in incument solutions
		
		int qntToBeSampled = (int)(0.25 * sortedTripleElements.length); // Number of candidates to be selected to diversification
		int qntToBeChosen = (int)(0.50 * qntToBeSampled); // Max number of selected candidates = half of the candidate list
		
		// Candidates to diversification, the 25% with less frequency
		ArrayList<TripleElement> worstTrilpleElements = new ArrayList<TripleElement> (Arrays.asList(Arrays.copyOfRange(sortedTripleElements, 0, qntToBeSampled)));
		Collections.shuffle(worstTrilpleElements);
		
		TL = makeTL(); // Restart the TL
		incumbentSol = createEmptySol(); // Restart the incumbent
		incumbentCost = Double.POSITIVE_INFINITY;
		updateCL(); // Restart CL
		
		int i = 0;
		int j = 0;
		
		while(i < qntToBeChosen && j < qntToBeSampled) // Insert at maximum qntToBeChosen elements, depending on conflicts related to prohibited triples
		{
			Integer candIn = worstTrilpleElements.get(i).getIndex();
			if(worstTrilpleElements.get(i).getAvailable()) // If the candidate randomly selected is available
			{
				// Add to TL
				TL.poll();
				TL.add(fake);
				TL.poll();
				TL.add(candIn);
				incumbentSol.add(candIn); // Add to incumbent
				ObjFunction.evaluate(incumbentSol);
				updateCL();
				i++;
			}
			j++;
		}
		for(TripleElement tripElem : tripleElements)
			tripElem.setIncumbentFrequency(0); // reset frequency
		constructiveHeuristic(); // makes a construction from the selected ones

	}
	
	/**
	 * This method invoke all structures of tabu method to realize a tabu search
	 */
	@Override
	public Solution<Integer> solve() {
		long beginTime = System.currentTimeMillis();
		
		// Initialize incumbent, CL, TL etc
		bestSol = createEmptySol();
		incumbentSol = createEmptySol();
		incumbentCost = Double.POSITIVE_INFINITY;
		CL = makeCL();
		RCL = makeRCL();
		constructiveHeuristic(); // Create initial solution
		TL = makeTL();
		
		double lastDiversification = 0; // Last time that diversification occurred
		double partialTime =  ((double)(System.currentTimeMillis() - beginTime) / 1000) / 60;
		int i = 0;
		
		// Stops by bestValue known or timeLimit or iterationsLimit
		while(valueLimit < bestSol.cost && (iterations <= 0 && partialTime <= timeLimit) || (iterations > 0 && i < iterations))
		{
			updateCL();

			// If diversification restart is activated and its time to diversify, it makes the process
			if(this.tabuStrategie == DIVERSIFICATION_RESTART && partialTime > lastDiversification + timeToDiversify)
			{
				System.out.println("DIVERSIFICOU " + partialTime + " " + i);
				diversify();
				lastDiversification = partialTime;
			}
			else // Else the standard neighborhood move is made
			{
				neighborhoodMove();
			}
			
			// Check if the incumbent is better than the best solution found
			if (bestSol.cost > incumbentSol.cost) {
				bestSol = new Solution<Integer>(incumbentSol);
				if (verbose)
					System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
			}
			
			// Increase frequency of elements at incumbent
			for (TripleElement tripElem : this.tripleElements) {
	        	if(tripElem.getSelected())
	        		tripElem.increaseFrequency();
	        }
			
			i++;
			partialTime =  ((double)(System.currentTimeMillis() - beginTime) / 1000) / 60;
		}

		return bestSol;
	}
}
