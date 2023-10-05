package es.uma.lcc.caesium.pedestrian.evacuation.optimization;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import static es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.statistics.Descriptive.*;
import static es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.statistics.Random.random;

/**
 * Evacuation problem: given an environment, find the location of a number
 * of exits in order to speed-up evacuation in case of emergency. Assumes 
 * that the environment has a single domain (in addition to the outside).
 * @author ccottap, ppgllrd
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
	private final Environment environment;
	/**
	 * number of exits
	 */
	private final int numExits;
	/**
	 * length of the perimeter
	 */
	private final double perimeterLength;
	/**
	 * Diameter of domain
	 */
	private final double diameter;
	/**
	 * width of exits
	 */
	private double exitWidth;
	/**
	 * A list of the exits initially contained in the environment (it may be empty). 
	 * These are kept fixed, and stored in order to restore the environment after adding 
	 * potential exits during simulation.
	 */
	private final ArrayList<Access> fixedAccesses;
	
	// TODO
	// There must be some member fields to account for the simulator and maybe
	// the simulator parameters
	
	/**
	 * Basic constructor
	 * @param env the environment
	 * @param numExits the number of exits
	 */
	public ExitEvacuationProblem(Environment env, int numExits) {
		assert env.getDomainsIDs().size() == 1 : "Too many domains";
		this.numExits = numExits;
		environment = env;
		Domain d = environment.getDomain(1); // assume a single domain
		perimeterLength = 2*(d.getHeight()+d.getWidth());
		diameter = Math.sqrt(Math.pow(d.getHeight(), 2) + Math.pow(d.getWidth(), 2));
		fixedAccesses = new ArrayList<>(environment.getDomain(1).getAccesses());
		setExitWidth(DEFAULT_EXIT_WIDTH); 
	}
	
	/**
	 * Creates the problem indicating the environment, the number of exits and their width
	 * @param env the environment
	 * @param numExits the number of exits
	 * @param width the exit width
	 */
	public ExitEvacuationProblem(Environment env, int numExits, double width) {
		this(env, numExits);
		setExitWidth(width);
	}
	
	/**
	 * Returns the number of exits
	 * @return the number of exits
	 */
	public int getNumExits() {
		return numExits;
	}

	/**
	 * Returns the perimeter length
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
	 * @param width the exit width to set
	 */
	public void setExitWidth(double width) {
		exitWidth = width;
	}
	
	
	/**
	 * Returns the width of the environment
	 * @return the width of the environment
	 */
	public double getWidth() {
		return environment.getDomain(1).getWidth();
	}
	
	
	/**
	 * Returns the height of the environment
	 * @return the height of the environment
	 */
	public double geHeight() {
		return environment.getDomain(1).getHeight();
	}

	// TODO make this a parameter
	private final double timeLimit = 1 * 60; // 1 minute is time limit for simulation

	/**
	 * Simulates the evacuation given the list of exits are added
	 * to the environment.
	 * @param accesses list of exits to be added to the environment
	 */
	public double simulate (List<Access> accesses) {
		var domain = environment.getDomain(1);
		var domainAccesses = domain.getAccesses();
		domainAccesses.addAll(accesses);

		// simulate

		// TODO set simulator parameters properly

		Scenario scenario = new Scenario.FromDomainBuilder(domain)
				.cellDimension(domain.getWidth() / 110)
				.floorField(DijkstraStaticFloorFieldWithMooreNeighbourhood::of)
				.build();

		var cellularAutomatonParameters =
				new CellularAutomatonParameters.Builder()
						.scenario(scenario) // use this scenario
						.timeLimit(timeLimit) // 1 minute is time limit for simulation
						.neighbourhood(MooreNeighbourhood::of) // use Moore's Neighbourhood for automaton
						.pedestrianVelocity(1.3) // fastest pedestrians walk at 1.3 m/s
						.build();

		var automaton = new CellularAutomaton(cellularAutomatonParameters);

		// place pedestrians
		Supplier<PedestrianParameters> pedestrianParametersSupplier = () ->
				new PedestrianParameters.Builder()
						.fieldAttractionBias(random.nextDouble(0.65, 2.0))
						.crowdRepulsion(random.nextDouble(1.00, 1.50))
						.velocityPercent(random.nextDouble(0.3, 1.0))
						.build();

		var numberOfPedestrians = 1000; // random.nextInt(150, 600);
		automaton.addPedestriansUniformly(numberOfPedestrians, pedestrianParametersSupplier);

		automaton.run();

		// TODO gather metrics from simulation

		domainAccesses.clear();
		domainAccesses.addAll(fixedAccesses);

		// TODO compute fitness
		return fitness(automaton);
	}

	public double fitness(CellularAutomaton automaton) {
		double f = automaton.numberOfNonEvacuees();
		if (f > 0) {
			var distances = automaton.distancesToClosestExit();
			f += minimum(distances) / diameter + mean(distances) / Math.pow(diameter, 2);
		} else {
			var times = automaton.evacuationTimes();
			f += maximum(times) / timeLimit + mean(times) / Math.pow(timeLimit, 2);
		}
		return f;
	}
	
	@Override
	public String toString() {
		return "Evacuation Problem\n------------------"
				+ "\nEnvironment:     " + environment.jsonPrettyPrinted()
				+ "\nNumber of exits: " + numExits
				+ "\nExit width:      " + exitWidth;
	}
}
