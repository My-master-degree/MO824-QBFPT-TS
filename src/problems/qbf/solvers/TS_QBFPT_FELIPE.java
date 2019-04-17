package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import solutions.Solution;
import triple.Triple;
import triple.TripleElement;

public class TS_QBFPT_FELIPE extends TS_QBF {
	
	/**
     * List of element objects used in prohibited triples. These objects
     * represents the variables of the model.
     */
    private TripleElement[] tripleElements;

    /**
     * List of prohibited triples.
     */
    private Triple[] triples; 
    
    public static final int STANDARD = 1;
    public static final int PROBABILISTIC  = 2;
    public static final int DIVERSIFICATION_RESTART = 3;
    private final int tabuStrategie;
    private final int iterationsToDiversify;
    
    public static final int FIRST_IMPROVEMENT = 1;
    public static final int BEST_IMPROVEMENT = 2;
    private final int localSearchStrategie;
    

	public TS_QBFPT_FELIPE(Integer tenure, Integer iterations, String filename, int tabuStrategie, int localSearchStrategie, int iterationsToDiversify) throws IOException {
		super(tenure, iterations, filename);
		// TODO Auto-generated constructor stub
		
		this.tabuStrategie = tabuStrategie;
		this.localSearchStrategie = localSearchStrategie;
		this.iterationsToDiversify = iterationsToDiversify;
		
		generateTripleElements();
        generateTriples();
	}
	
    /**
     * A GRASP CL generator for MAXQBFPT problem
     *
     * @return A list of candidates to partial solution
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
	

	@Override
    public void updateCL() {
        ArrayList<Integer> _CL = new ArrayList<Integer>();
        
        for (TripleElement tripElem : this.tripleElements) {
        	tripElem.setSelected(false);
        	tripElem.setAvailable(true);
        }
        
        if (this.incumbentSol != null) {
            for (Integer e : this.incumbentSol) {
                this.tripleElements[e].setSelected(true);
                this.tripleElements[e].setAvailable(false);
            }
        }

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
	
	@Override
	public Solution<Integer> neighborhoodMove() {
		ArrayList <ArrayList<Integer>> neighborhood = getNeighborhood();
		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;
		minDeltaCost = Double.POSITIVE_INFINITY;
		Integer candIn, candOut;
		
		for(int i = 0; i < neighborhood.size(); i++)
		{
			candIn = neighborhood.get(i).get(0);
			candOut = neighborhood.get(i).get(1);
			Double deltaCost;
			if(candOut == fake)
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
			else if(candIn == fake)
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
			else
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
			if(this.localSearchStrategie == FIRST_IMPROVEMENT && deltaCost < 0)
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
	
	private ArrayList <ArrayList<Integer>> getNeighborhood()
	{
		ArrayList <ArrayList<Integer>> neighborhood = new ArrayList<ArrayList<Integer>>();
		
		for(Integer i : CL)
		{
			neighborhood.add(new ArrayList<Integer>(Arrays.asList(i, fake)));
			
			for (Integer j : incumbentSol) {
				neighborhood.add(new ArrayList<Integer>(Arrays.asList(i, j)));
            }
		}
		
		for (Integer j : incumbentSol) {
			neighborhood.add(new ArrayList<Integer>(Arrays.asList(fake, j)));
        }
		
		
		
		if(this.tabuStrategie == PROBABILISTIC)
		{
			Collections.shuffle(neighborhood);
			int lastIndex = (int) (neighborhood.size() * 0.50) - 1;
			ArrayList <ArrayList<Integer>> probabilisticNeighborhood =  new ArrayList<ArrayList<Integer>>(neighborhood.subList(0, lastIndex));
			return probabilisticNeighborhood;
		}
		
		if(this.localSearchStrategie == FIRST_IMPROVEMENT)
			Collections.shuffle(neighborhood);
		
		return neighborhood;
	}
	
	public void diversify()
	{
		TripleElement[] sortedTripleElements = tripleElements.clone();
		int qntToBeChosen = (int)(0.10 * sortedTripleElements.length);
		int qntToBeSampled = (int)(0.25 * sortedTripleElements.length);

		Arrays.sort(sortedTripleElements, Comparator.comparing(TripleElement::getIncumbentFrequency));
		ArrayList<TripleElement> worstTrilpleElements = new ArrayList<TripleElement> (Arrays.asList(Arrays.copyOfRange(sortedTripleElements, 0, qntToBeSampled)));
		Collections.shuffle(worstTrilpleElements);
		
		int i = 0;
		int j = 0;
		
		while(i < qntToBeChosen && j < qntToBeSampled)
		{
			Integer candIn = worstTrilpleElements.get(i).getIndex();
			if(worstTrilpleElements.get(i).getAvailable())
			{
				TL.poll();
				TL.add(fake);
				TL.poll();
				TL.add(candIn);
				incumbentSol.add(candIn);
				ObjFunction.evaluate(incumbentSol);
				updateCL();
				i++;
			}
			j++;
		}
		
	}
	
	@Override
	public Solution<Integer> solve() {

		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
		
		for (int i = 0; i < iterations; i++) {
			
			updateCL();
			
			if(this.tabuStrategie == DIVERSIFICATION_RESTART && i > 0 && i % this.iterationsToDiversify == 0)
				diversify();
			else
				neighborhoodMove();
			
			if (bestSol.cost > incumbentSol.cost) {
				bestSol = new Solution<Integer>(incumbentSol);
				if (verbose)
					System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
			}
			else if (i % 100000 == 0)
			{
				System.out.println("(Iter. " + i + ") BestSol not improved");

			}
			
			for (TripleElement tripElem : this.tripleElements) {
	        	if(tripElem.getSelected())
	        		tripElem.increaseFrequency();
	        }
		}

		return bestSol;
	}
	
	
	
	/**
	 * A main method used for testing the TS metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		TS_QBF tabusearch = new TS_QBFPT_FELIPE(20, 1000000, "instances/qbf040", DIVERSIFICATION_RESTART, FIRST_IMPROVEMENT, 1000);
		Solution<Integer> bestSol = tabusearch.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

	}

}
