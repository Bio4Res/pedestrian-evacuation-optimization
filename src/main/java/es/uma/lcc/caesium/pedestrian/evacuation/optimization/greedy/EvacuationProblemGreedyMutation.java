package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.mutation.MutationOperator;
import es.uma.lcc.caesium.ea.operator.variation.mutation.continuous.GaussianMutation;
import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.PerimetralExitOptimizationFunction;

/**
 * Mutates a solution using a greedy procedure with some probability. The
 * greedy procedure picks k exits (k being a parameter) and replaces them 
 * greedily. Alternatively, a standard Gaussian mutation is used.
 * @author ccottap
 * @version 1.0
 *
 */
public class EvacuationProblemGreedyMutation extends MutationOperator {
	/**
	 * Internal mutation operator
	 */
	private GaussianMutation op; 
	/**
	 * probability of applying the greedy mutation
	 */
	private double greedyProb;
	/**
	 * the greedy construction procedure
	 */
	private GreedyPerimetralExitPlacement gpep;
	/**
	 * number of exists to be greedily mutated
	 */
	private int numExitsMutated;
	/**
	 * equivalent cost in evaluation calls of an application of the greedy initialization
	 */
	private double extra;

	
	/**
	 * Creates the operator. 
	 * @param pars parameters: application probability, the probability of applying the greedy mutation, the number of exits to mutate, gaussian mutation parameters
	 */
	public EvacuationProblemGreedyMutation(List<String> pars) {
		super(pars);
		greedyProb = Double.parseDouble(pars.get(1));
		numExitsMutated = Integer.parseInt(pars.get(2));
		gpep = null;
		var params = new ArrayList<String>();
		params.add("1.0"); 			// if we decide not to use greedy mutation, we always use the Gaussian mutation
		params.add(pars.get(3));	// amplitude of the mutation
		params.add("true");			// wrap around the min/max limits
		op = new GaussianMutation(params);
		extra = 0.0;
	}
	
	/**
	 * Sets the objective function for potential use inside some operator
	 * @param obj the objective function
	 */
	public void setObjectiveFunction (ObjectiveFunction obj) {
		super.setObjectiveFunction(obj);
		op.setObjectiveFunction(obj);
		PerimetralExitOptimizationFunction peof = (PerimetralExitOptimizationFunction)obj;
		gpep = new GreedyPerimetralExitPlacement(peof.getExitEvacuationProblem());
		gpep.setVerbosityLevel(0);
		extra = peof.getExitEvacuationProblem().getPerimeterLength()/peof.getExitEvacuationProblem().getExitWidth();  // extra cost per mutated exit
	}
	
	@Override
	public Individual _apply(List<Individual> parents) {
		Individual ind;
		if (EAUtil.random01() < greedyProb) {
			int l = obj.getNumVars();
			int num = (numExitsMutated <= 0) ? l : numExitsMutated; // values <= 0 are used to denoted that all exits will be mutated
			ind = parents.get(0).clone();
			Genotype g = ind.getGenome();
			var mutateOrder = EAUtil.randomPermutation(l);
			var fixed = new ArrayList<Double>(l);
			for (int i=num; i<l; i++)
				fixed.add((double)g.getGene(mutateOrder.get(i)));
			for (int i=0; i<num; i++) {
				fixed.add(gpep.next(fixed));
			}
			for (int i=0; i<l; i++)
				g.setGene(i, fixed.get(i));
			obj.addExtraCost(num*extra - 1.0);  // deducts 1.0 because the solution is technically evaluated
			ind.touch();
		}
		else 
			ind = op.apply(parents);
				
		return ind;
	}

	
	@Override
	public String toString() {
		return "GreedyExitPlacement";
	}
	

}
