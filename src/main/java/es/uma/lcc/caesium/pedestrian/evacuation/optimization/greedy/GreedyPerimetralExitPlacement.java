package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.SimulationSummary;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;

/**
 * A greedy algorithm to place exits in the best position out of
 * a collection a_0, ..., a_m, where a_0 is randomly chosen and 
 * a_{i+1} = a_i + exit_width (the addition being treated as circular 
 * within the range of the perimeter.
 * @author ccottap
 * @version 1.0 
 */
public class GreedyPerimetralExitPlacement {
	/**
	 * Width of exits in meters
	 */
	private final double exitWidth;
	/**
	 * length of the perimeter
	 */
	private final double perimeterLength;
	/**
	 * number of potential locations for an exit
	 */
	private final int numpos;
	/**
	 * normalized displacement for each potential exit location
	 */
	private final double inc;
	/**
	 * the instance of the evacuation problem
	 */
	private final ExitEvacuationProblem eep;
	/**
	 * decoder of accesses
	 */
	private Double2AccessDecoder decoder;
	/**
	 * verbosityL level (0 = no verbosity)
	 */
	private int verbosityLevel = 0; 
	
	/**
	 * Creates the greedy algorithm
	 * @param eep an exit evacuation problem
	 */
	public GreedyPerimetralExitPlacement(ExitEvacuationProblem eep) {
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
		exitWidth = eep.getExitWidth();
		perimeterLength = eep.getPerimeterLength();	
		numpos = (int)Math.ceil(perimeterLength/exitWidth);
		inc = 1.0/numpos;
	}

	/**
	 * Sets the verbosity level
	 * @param verbosityLevel the verbosity level to set
	 */
	public void setVerbosityLevel(int verbosityLevel) {
		this.verbosityLevel = verbosityLevel;
	}

	/**
	 * Finds the location of the next exit
	 * @param current the current list of exits
	 * @return the location of the next exit
	 */
	public double nextExit(List<Access> current) {
		double pos = EAUtil.random01();
		int id = current.size();
		double best = Double.POSITIVE_INFINITY;
		double bestpos = -1;
		if (verbosityLevel > 0) {
			System.out.println("Trying " + current.size() + "...");
			System.out.println("Initial: " + eep.fitness(eep.simulate(current)));
		}
		for (int i=0; i<numpos; i++) {
			double location = pos * (perimeterLength - exitWidth);
			List<Access> acc = new ArrayList<Access>(current);
			acc.addAll(decoder.decodeAccess(location, id, id));
			SimulationSummary summary = eep.simulate(acc);
			double quality = eep.fitness(summary);
			if (quality < best) {
				best = quality;
				bestpos = pos;
				if (verbosityLevel > 0)
					System.out.println("New best: " + pos + " (" + best + ")");
			}
			pos += inc;
			if (pos > 1.0)
				pos -= 1.0;
		}
		if (verbosityLevel > 0)
			System.out.println("Final best: " + bestpos + " (" + best + ")");
		return bestpos;
	}
	
	
	/**
	 * Finds the location of the next exit
	 * @param current the current list of exits (expressed as normalized points in the perimeter)
	 * @return the location of the next exit
	 */
	public double next(List<Double> locations) {
		List<Access> current = new ArrayList<Access>();
		int id = 0;
	    for (double loc : locations) {
	    	current.addAll(decoder.decodeAccess(loc* (perimeterLength - exitWidth), id++, current.size()));
	    }
		double pos = EAUtil.random01();
		id = current.size();
		double best = Double.POSITIVE_INFINITY;
		double bestpos = -1;
		
		if (verbosityLevel > 0)
			System.out.println("Trying " + current.size() + "...");
		
		for (int i=0; i<numpos; i++) {
			double location = pos * (perimeterLength - exitWidth);
			List<Access> acc = new ArrayList<Access>(current);
			acc.addAll(decoder.decodeAccess(location, id, id));
			SimulationSummary summary = eep.simulate(acc);
			double quality = eep.fitness(summary);
			if (quality < best) {
				best = quality;
				bestpos = pos;
				if (verbosityLevel > 0)
					System.out.println("New best: " + pos + " (" + best + ") ");
			}
			pos += inc;
			if (pos > 1.0)
				pos -= 1.0;
		}
		if (verbosityLevel > 0)
			System.out.println("Final best: " + bestpos + " (" + best + ")");
		return bestpos;
	}
	
	/**
	 * Greedily finds the position of a number of exits
	 * @param numExits the number of exits to be found
	 * @return the list of encoded locations
	 */
	public List<Double> getExits (int numExits) {
		List<Double> locations = new ArrayList<Double>(numExits);
		List<Access> acc = new ArrayList<Access>(numExits);
		for (int i=0; i<numExits; i++) {
			double next = nextExit (acc);
			acc.addAll(decoder.decodeAccess(next * (perimeterLength - exitWidth), i, acc.size()));
			locations.add(next);
		}
		
		return locations;
	}
	
}
