package es.uma.lcc.caesium.pedestrian.evacuation.optimization;


import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;


/**
 * Objective function of the EA in order to find the location of a certain fixed
 * number of exits in the perimeter of a given environment, so that the evacuation 
 * performance is optimized.
 * @author ccottap, ppgllrd
 * @version 1.1
 *
 */
public class PerimetralExitOptimizationFunction extends ContinuousObjectiveFunction {
	/**
	 * Width of exits in meters
	 */
	private final double exitWidth;
	/**
	 * number of exits
	 */
	private final int numExits;
	/**
	 * length of the perimeter
	 */
	private final double perimeterLength;
	/**
	 * the instance of the evacuation problem
	 */
	private final ExitEvacuationProblem eep;
	/**
	 * decoder of accesses
	 */
	private Double2AccessDecoder decoder;
	
	/**
	 * Basic constructor
	 * @param eep the evacuation problem
	 */
	public PerimetralExitOptimizationFunction(ExitEvacuationProblem eep) {
		super(eep.getNumExits(), 0.0, 1.0);
		numExits = eep.getNumExits();
		perimeterLength = eep.getPerimeterLength();
		exitWidth = eep.getExitWidth();
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
	}
	
	/**
	 * Indicates whether the goal is maximization or minimization
	 * @return the optimization sense
	 */
	public OptimizationSense getOptimizationSense()
	{
		return OptimizationSense.MINIMIZATION;
	}


	@Override
	protected double _evaluate(Individual ind) {
		List<Access> exits = new ArrayList<>(numExits);
		Genotype g = ind.getGenome();
		var id = 0;
		for (int exit=0; exit<numExits; exit++) {
			double location = ((double)g.getGene(exit))*(perimeterLength-exitWidth);
			exits.addAll(decoder.decodeAccess(location, exit, id));
			id = exits.size();
		}
		// simulate
		SimulationSummary summary = eep.simulate(exits);
		return eep.fitness (summary);
	}
	
}
