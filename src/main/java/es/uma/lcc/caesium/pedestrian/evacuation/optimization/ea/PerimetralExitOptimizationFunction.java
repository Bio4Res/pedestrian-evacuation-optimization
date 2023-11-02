package es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;


/**
 * Objective function of the EA in order to find the location of a certain fixed
 * number of exits in the perimeter of a given environment, so that the evacuation 
 * performance is optimized.
 * @author ccottap, ppgllrd
 * @version 1.2
 *
 */
public class PerimetralExitOptimizationFunction extends ContinuousObjectiveFunction {
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
	 * cache of fitness evaluations
	 */
	private HashMap<TreeSet<Double>, Double> cache;
	/**
	 * granularity in the location of exits
	 */
	private static final double EXIT_PRECISION = 0.1;
	/**
	 * used to round off location values
	 */
	private static final double FACTOR = 1.0 / EXIT_PRECISION;
	
		
	/**
	 * Basic constructor
	 * @param eep the evacuation problem
	 */
	public PerimetralExitOptimizationFunction(ExitEvacuationProblem eep) {
		super(eep.getNumExits(), 0.0, 1.0);
		numExits = eep.getNumExits();
		perimeterLength = eep.getPerimeterLength();
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
		cache = new HashMap<TreeSet<Double>, Double>();
	}
	
	
	/**
	 * Returns the exit evacuation problem being solved
	 * @return the exit evacuation problem being solved
	 */
	public ExitEvacuationProblem getExitEvacuationProblem() {
		return eep;
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
		TreeSet<Double> genes = individualToTreeSet (ind);
		Double val = cache.get(genes);
		if (val == null) {
			val = eep.fitness (eep.simulate (decode (ind)));
			cache.put(genes, val);
		}
		
		return val;
	}
	
	/**
	 * Transforms an individual's genome into a tree set (because genome ordering is irrelevant
	 * when it comes to compare solutions).
	 * @param ind an individual
	 * @return a tree set with the individual's genes
	 */
	private TreeSet<Double> individualToTreeSet (Individual ind) {
		TreeSet<Double> genes = new TreeSet<Double>();
		Genotype g = ind.getGenome();
		for (int exit=0; exit<numExits; exit++) {
			genes.add(roundLocation(g, exit));
		}
		return genes;
	}
	


	/**
	 * Decodes an individual, transforming each gene into the corresponding access(es).
	 * @param ind an individual
	 * @return the list f accesses encoded in the individual's genome.
	 */
	public List<Access> decode (Individual ind) {
		List<Access> exits = new ArrayList<>(numExits);
		Genotype g = ind.getGenome();
		var id = 0;
		for (int exit=0; exit<numExits; exit++) {
			// locations have a precision of 1cm
			double location = roundLocation(g, exit);
			exits.addAll(decoder.decodeAccess(location, exit, id));
			id = exits.size();
		}
		return exits;
	}
	
	/**
	 * Rounds off the value of a certain exit to the desired precision
	 * @param g the genotype
	 * @param index index of the exit
	 * @return the location along the perimeter (rounded off).
	 */
	private double roundLocation (Genotype g, int index) {
		return Math.round(((double)g.getGene(index)) * perimeterLength * FACTOR)/FACTOR;
	}
	
}
