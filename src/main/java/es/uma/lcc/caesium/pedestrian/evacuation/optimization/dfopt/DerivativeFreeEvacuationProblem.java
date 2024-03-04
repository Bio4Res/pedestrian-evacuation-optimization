package es.uma.lcc.caesium.pedestrian.evacuation.optimization.dfopt;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.dfopt.base.DerivativeFreeObjectiveFunction;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;

/**
 * Evacuation Problem for derivative-free optimization
 * @author ccottap
 * @version 1.1
 */
public class DerivativeFreeEvacuationProblem extends DerivativeFreeObjectiveFunction {
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
	public DerivativeFreeEvacuationProblem(ExitEvacuationProblem eep) {
		numExits = eep.getNumExits();
		perimeterLength = eep.getPerimeterLength();
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
	}
	
	
	
	@Override
	protected double _evaluate(List<Double> sol) {
		assert sol.size() == numExits;
		return eep.fitness (eep.simulate (decode (sol)));
	}
	
	/**
	 * Decodes a solution, transforming each double in [0,1] into the corresponding access(es).
	 * @param sol a list of exit locations
	 * @return the list f accesses encoded in the solution
	 */
	public List<Access> decode (List<Double> sol) {
		List<Access> exits = new ArrayList<>(numExits);
		var id = 0;
		for (int exit=0; exit<numExits; exit++) {
			// locations have a precision of 1cm
			double location = roundLocation(sol, exit);
			exits.addAll(decoder.decodeAccess(location, exit, id));
			id = exits.size();
		}
		return exits;
	}
	
	/**
	 * Rounds off the value of a certain exit to the desired precision
	 * @param sol the exits
	 * @param index index of the exit
	 * @return the location along the perimeter (rounded off).
	 */
	private double roundLocation (List<Double> sol, int index) {
		return Math.round(sol.get(index) * perimeterLength * FACTOR)/FACTOR;
	}

	@Override
	public int getNumVariables() {
		return numExits;
	}

	@Override
	public double getMinValue(int i) {
		return 0.0;
	}

	@Override
	public double getMaxValue(int i) {
		return 1.0;
	}



}
