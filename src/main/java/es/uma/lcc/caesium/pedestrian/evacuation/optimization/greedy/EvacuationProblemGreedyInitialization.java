package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.initialization.InitializationOperator;
import es.uma.lcc.caesium.ea.operator.variation.initialization.continuous.RandomVector;
import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.PerimetralExitOptimizationFunction;

/**
 * Constructs a greedy solution with some probability. Otherwise,
 * a purely random solution is created
 * @author ccottap
 * @version 1.0
 *
 */
public class EvacuationProblemGreedyInitialization extends InitializationOperator {
	/**
	 * Internal variation operator
	 */
	private RandomVector op; 
	/**
	 * probability of applying the greedy initialization
	 */
	private double greedyProb;
	/**
	 * the greedy construction procedure
	 */
	private GreedyPerimetralExitPlacement gpep;
	/**
	 * equivalent cost in evaluation calls of an application of the greedy initialization
	 */
	private double extra;

	
	/**
	 * Creates the operator. 
	 * @param pars the probability of applying the greedy initialization
	 */
	public EvacuationProblemGreedyInitialization(List<String> pars) {
		super(new ArrayList<String>(0));
		greedyProb = Double.parseDouble(pars.get(0));
		gpep = null;
		op = new RandomVector(new ArrayList<String>(0));
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
		extra = peof.getNumVars() * peof.getExitEvacuationProblem().getPerimeterLength()/peof.getExitEvacuationProblem().getExitWidth() - 1.0;
	}
	
	@Override
	public Individual _apply(List<Individual> parents) {
		Individual ind;
		if (EAUtil.random01() < greedyProb) {
			int l = obj.getNumVars();
			List<Double> locations = gpep.getExits(l);
			Genotype g = new Genotype(l);
			for (int i=0; i<l; i++)
				g.setGene(i, locations.get(i));
			ind = new Individual();
			ind.setGenome(g);
			obj.addExtraCost(extra);
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
