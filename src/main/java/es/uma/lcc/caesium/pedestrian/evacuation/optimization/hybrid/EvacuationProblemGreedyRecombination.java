package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.util.HashSet;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.recombination.discrete.set.FixedSizeSetRecombination;
import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.PerimetralExitOptimizationFunction;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy.GreedyPerimetralExitPlacement;

/**
 * Recombines a collection of parents using a greedy procedure with some probability. The
 * greedy procedure creates a set with all the exits in the parents and picks a subset
 * of the desired size. Alternatively, a random set recombination is used.
 * @author ccottap
 * @version 1.0
 *
 */
public class EvacuationProblemGreedyRecombination extends FixedSizeSetRecombination {
	/**
	 * probability of applying the greedy recombination
	 */
	private double greedyProb;
	/**
	 * the greedy construction procedure
	 */
	private GreedyPerimetralExitPlacement gpep;

	
	
	/**
	 * Creates the operator. 
	 * @param pars parameters: application probability, arity, the probability of applying the greedy mutation
	 */
	public EvacuationProblemGreedyRecombination(List<String> pars) {
		super(pars);
		greedyProb = Double.parseDouble(pars.get(2));
		gpep = null;
	}
	
	/**
	 * Sets the objective function for potential use inside some operator
	 * @param obj the objective function
	 */
	public void setObjectiveFunction (ObjectiveFunction obj) {
		super.setObjectiveFunction(obj);
		PerimetralExitOptimizationFunction peof = (PerimetralExitOptimizationFunction)obj;
		gpep = new GreedyPerimetralExitPlacement(peof.getExitEvacuationProblem());
		gpep.setVerbosityLevel(0);
	}
	
	@Override
	public Individual _apply(List<Individual> parents) {
		Individual ind;
		if (EAUtil.random01() < greedyProb) {
			int l = obj.getNumVars();
			var union = new HashSet<Double>();
			for (Individual p: parents) {
				Genotype g = p.getGenome(); 
				for (int i=0; i<l; i++)
					union.add((double)g.getGene(i));
			}
			int s = union.size();
			for (int i=0; i<l; i++)
				obj.addExtraCost(s-i);  
			List<Double> selected = gpep.getExits(l, union);
			Genotype offspring = new Genotype(l);
			for (int i=0; i<l; i++)
				offspring.setGene(i, selected.get(i));
			ind = new Individual();
			ind.setGenome(offspring);
		}
		else 
			ind = super._apply(parents);

		return ind;
	}

	
	@Override
	public String toString() {
		return "GreedyExitRecombination(" + super.toString() + ")";
	}

}
