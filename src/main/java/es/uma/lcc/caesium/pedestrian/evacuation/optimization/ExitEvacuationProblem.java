package es.uma.lcc.caesium.pedestrian.evacuation.optimization;


import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Evacuation problem: given an environment, find the location of a number
 * of exits in order to speed-up evacuation in case of emergency. Assumes 
 * that the environment has a single domain (in addition to the outside).
 * @author ccottap
 * @version 1.0
 *
 */
public class ExitEvacuationProblem {
	/**
	 * Default width of exits in meters
	 */
	private static final double DEFAULT_EXIT_WIDTH = 2.0;
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
	 * width of exits
	 */
	private double exitWidth;
	/**
	 * A list of the exits initially contained in the environment (it may be empty). 
	 * These are kept fixed, and stored in order to restore the environment after adding 
	 * potential exits during simulation.
	 */
	private ArrayList<Access> current;
	
	// TODO
	// There must be some member fields to account for the simulator and maybe
	// the simulator parameters
	
	/**
	 * Basic constructor
	 * @param env the environment
	 * @param i the number of exits
	 */
	public ExitEvacuationProblem(Environment env, int i) {
		assert env.getDomainsIDs().size() == 1 : "Too many domains";
		numExits = i;
		base = env;
		Domain d = base.getDomain(1); // assume a single domain
		perimeterLength = 2*(d.getHeight()+d.getWidth());
		current = new ArrayList<Access>(base.getDomain(1).getAccesses());
		setExitWidth(DEFAULT_EXIT_WIDTH); 
	}
	
	/**
	 * Creates the problem indicating the environment, the number of exits and their width
	 * @param env the environment
	 * @param i the number of exits
	 * @param e the exit width
	 */
	public ExitEvacuationProblem(Environment env, int i, double e) {
		this(env, i);
		setExitWidth(e);
	}
	
	/**
	 * Returns the number of exits
	 * @return the number of exits
	 */
	public int getNumExits() {
		return numExits;
	}

	/**
	 * Returns the the perimeter length
	 * @return the perimeter length
	 */
	public double getPerimeterLength() {
		return perimeterLength;
	}


	/**
	 * Return the exit width
	 * @return the exit width
	 */
	public double getExitWidth() {
		return exitWidth;
	}


	/**
	 * Sets the exit width
	 * @param e the exit width to set
	 */
	public void setExitWidth(double e) {
		exitWidth = e;
	}
	
	
	/**
	 * Returns the width of the environment
	 * @return the width of the environment
	 */
	public double getWidth() {
		return base.getDomain(1).getWidth();
	}
	
	
	/**
	 * Returns the height of the environment
	 * @return the height of the environment
	 */
	public double geHeight() {
		return base.getDomain(1).getHeight();
	}
	
	
	/**
	 * Simulates the evacuation given the list of exits are added
	 * to the environment.
	 * @param accesses list of exits to be added to the environment
	 */
	public void simulate (List<Access> accesses) {
		// TODO
		// Complete this method with the simulation
		// A suitable class or record can be created to return the outcome
		// of the simulation, or separate methods can be created to obtain 
		// different performance indicators of the simulator (number of people
		// that got out, time of the last person, ...)
		base.getDomain(1).getAccesses().addAll(accesses);
		// simulate
		base.getDomain(1).getAccesses().clear();
		base.getDomain(1).getAccesses().addAll(current);
	}
	
	
	@Override
	public String toString() {
		String str = "Evacuation Problem\n------------------" 
				+ "\nEnvironment:     " + base.jsonPrettyPrinted() 
				+ "\nNumber of exits: " + numExits
				+ "\nExit width:      " + exitWidth;
		
		return str;
	}


}
