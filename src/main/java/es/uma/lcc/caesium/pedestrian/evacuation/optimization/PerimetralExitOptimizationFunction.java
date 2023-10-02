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
 * @author ccottap
 * @version 1.0
 *
 */
public class PerimetralExitOptimizationFunction extends ContinuousObjectiveFunction {
	/**
	 * Width of exits in meters
	 */
	private double exitWidth;
	/**
	 * number of exits
	 */
	private int numExits;
	/**
	 * length of the perimeter
	 */
	private double perimeterLength;
	/**
	 * Width of the environment
	 */
	private double width;
	/**
	 * height of the environment
	 */
	private double height;
	/**
	 * the instance of the evacuation problem
	 */
	private ExitEvacuationProblem eep;
	
	/**
	 * Basic constructor
	 * @param eep the evacuation problem
	 */
	public PerimetralExitOptimizationFunction(ExitEvacuationProblem eep) {
		super(eep.getNumExits(), 0.0, 1.0);
		numExits = eep.getNumExits();
		perimeterLength = eep.getPerimeterLength();
		exitWidth = eep.getExitWidth();
		width = eep.getWidth();
		height = eep.geHeight();
		this.eep = eep;
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
		List<Access> exits = new ArrayList<Access> (numExits);
		// TODO Complete this method 
		Genotype g = ind.getGenome();
		for (int i=0; i<numExits; i++) {
			double location = ((double)g.getGene(i))*(perimeterLength-exitWidth);
			// create access and add to list
			// consider width and height to account for exits that are across a corner of the environment
		}
		// simulate
		eep.simulate(exits);
		// compute fitness
		return 0.0;
	}
	
	
	
}
