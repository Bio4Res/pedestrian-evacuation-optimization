package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.initialization.continuous.RandomVector;
import es.uma.lcc.caesium.ea.operator.variation.mutation.continuous.GaussianMutation;
import es.uma.lcc.caesium.ea.util.EAUtil;

/**
 * Mutates a solution using a greedy procedure with some probability. The
 * greedy procedure generates a random solution and performs a greedy recombination. 
 * Alternatively, a standard Gaussian mutation is used.
 * @author ccottap
 * @version 1.0
 *
 */
public class EvacuationProblemGreedyMacroMutation extends GaussianMutation {
	/**
	 * probability of applying the greedy mutation
	 */
	private double greedyProb;
	/**
	 * internal greedy recombination operator
	 */
	private EvacuationProblemGreedyRecombination crossover;
	/**
	 * internal random initialization operator
	 */
	private RandomVector create;

	
	/**
	 * Creates the operator. 
	 * @param pars parameters: application probability, Gaussian mutation parameters (amplitude and wrapping), the probability of applying the greedy mutation, the number of exits to mutate
	 */
	public EvacuationProblemGreedyMacroMutation(List<String> pars) {
		super(pars);
		greedyProb = Double.parseDouble(pars.get(3));
		var params = new ArrayList<String>();
		params.add("1.0");
		params.add("2");
		params.add("1.0");
		crossover = new EvacuationProblemGreedyRecombination(params);
		params.clear();
		create = new RandomVector(params);
	}
	
	/**
	 * Sets the objective function for potential use inside some operator
	 * @param obj the objective function
	 */
	public void setObjectiveFunction (ObjectiveFunction obj) {
		super.setObjectiveFunction(obj);
		crossover.setObjectiveFunction(obj);
		create.setObjectiveFunction(obj);
	}
	
	@Override
	public Individual _apply(List<Individual> parents) {
		Individual ind;
		if (EAUtil.random01() < greedyProb) {
			List<Individual> pool = new ArrayList<Individual>();
			pool.add(parents.get(0));
			pool.add(create.apply(new ArrayList<Individual>(0)));
			ind = crossover._apply(pool);
			ind.touch();
		}
		else  {
			ind = super._apply(parents);
		}
		return ind;
	}

	
	@Override
	public String toString() {
		return "GreedyExitMacroMutation(" + super.toString() + ", " + greedyProb + ")";
	}
	

}
