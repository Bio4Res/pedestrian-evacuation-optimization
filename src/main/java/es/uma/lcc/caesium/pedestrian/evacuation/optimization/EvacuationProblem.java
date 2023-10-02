package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Evacuation problem. Given an environment, find the location of a number
 * of exits in order to speed-up evacuation in case of emergency
 * @author ccottap
 * @version 1.0
 *
 */
public class EvacuationProblem extends ContinuousObjectiveFunction {
	/**
	 * Width of exits in meters
	 */
	private static final double EXIT_WIDTH = 2.0;
	/**
	 * the environment whose evacuation is optimized
	 */
	private Environment base;
	/**
	 * number of exits
	 */
	private int numExits;
	/**
	 * length of the perimeter
	 */
	private double perimeterLength;
	
	/**
	 * Basic constructor
	 * @param i the number of exits
	 * @param env the environment
	 */
	public EvacuationProblem(int i, Environment env) {
		super(i, 0.0, 1.0);
		numExits = i;
		base = env;
		Domain d = base.getDomain(1); // assume a single domain
		perimeterLength = 2*(d.getHeight()+d.getWidth());
	}
	
	/**
	 * Indicates whether the goal is maximization or minimization
	 * @return the optimization sense
	 */
	public OptimizationSense getOptimizationSense()
	{
		return OptimizationSense.MAXIMIZATION;
	}
	
	@Override
	protected double _evaluate(Individual ind) {
		// TODO Complete this method 
		Genotype g = ind.getGenome();
		for (int i=0; i<numExits; i++) {
			double location = ((double)g.getGene(i))*(perimeterLength-EXIT_WIDTH);
			// add exit
		}
		// simulate
		// compute fitness
		return 0.0;
	}

}
