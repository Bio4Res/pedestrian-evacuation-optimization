package es.uma.lcc.caesium.pedestrian.evacuation.optimization;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithVonNewmanNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.FloorField;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.ManhattanStaticFloorField;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.Neighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.VonNeumannNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import static es.uma.lcc.caesium.statistics.Descriptive.*;
import static es.uma.lcc.caesium.statistics.Random.random;

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
	/**
	 * the parameters used by the simulator
	 */
	private final SimulationConfiguration simulationConf;

	/**
	 * the domain where sim,ulation takes place (assumes a single domain)
	 */
	private final Domain domain;

	/**
	 * the time limit for simulation
	 */
	private final double timeLimit;

	/**
	 * the type of static floor field to use
	 */
	private final Function<Scenario, FloorField> floorField;

	/**
	 * the type of neighbourhood to use in automaton
	 */
	private final Function<Scenario, Neighbourhood> neighbourhood;

	/**
	 * the number of independent simulations to run for each evaluation
	 */
	private final int numSimulations;

	/**
	 * the remaining parameters for the simulation
	 */
	private final double cellDimension, pedestrianReferenceVelocity, attractionBiasMin, attractionBiasMax,
			crowdRepulsionMin, crowdRepulsionMax, velocityFactorMin, velocityFactorMax;
	private final int numPedestriansMin, numPedestriansMax;

	/**
	 * Basic constructor
	 * @param environment the environment
	 * @param numExits the number of exits
	 * @param width the exit width
	 * @param simulationConf the parameters used by the simulator
	 */
	public ExitEvacuationProblem(Environment environment, int numExits, double width, SimulationConfiguration simulationConf) {
		assert environment.getDomainsIDs().size() == 1 : "Too many domains";
		this.environment = environment;
		this.numExits = numExits;
		this.simulationConf = simulationConf;
		setExitWidth(width);
		domain = environment.getDomain(1); // assume a single domain
		perimeterLength = 2*(domain.getHeight()+domain.getWidth());
		diameter = Math.sqrt(Math.pow(domain.getHeight(), 2) + Math.pow(domain.getWidth(), 2));
		fixedAccesses = new ArrayList<>(this.environment.getDomain(1).getAccesses());
		timeLimit = simulationConf.getDouble("timeLimit");
		numSimulations = simulationConf.getInt("numSimulations");
		cellDimension = simulationConf.getDouble("cellularAutomatonParameters/cellDimension");
		pedestrianReferenceVelocity = simulationConf.getDouble("crowd/pedestrianReferenceVelocity");
		attractionBiasMin = simulationConf.getDouble("crowd/attractionBias/min");
		attractionBiasMax = simulationConf.getDouble("crowd/attractionBias/max");
		crowdRepulsionMin = simulationConf.getDouble("crowd/crowdRepulsion/min");
		crowdRepulsionMax = simulationConf.getDouble("crowd/crowdRepulsion/max");
		velocityFactorMin = simulationConf.getDouble("crowd/velocityFactor/min");
		velocityFactorMax = simulationConf.getDouble("crowd/velocityFactor/max");
		numPedestriansMin = simulationConf.getInt("crowd/numPedestrians/min");
		numPedestriansMax = simulationConf.getInt("crowd/numPedestrians/max");
		floorField =
				switch (simulationConf.getString("cellularAutomatonParameters/floorField")) {
					case "DijkstraStaticMoore" -> DijkstraStaticFloorFieldWithMooreNeighbourhood::of;
					case "DijkstraStaticVonNeumann" -> DijkstraStaticFloorFieldWithVonNewmanNeighbourhood::of;
					case "ManhattanStatic" -> ManhattanStaticFloorField::of;
					default -> throw new IllegalArgumentException("Invalid floor field in configuration");
				};
		neighbourhood =
				switch (simulationConf.getString("cellularAutomatonParameters/neighborhood")) {
					case "Moore" -> MooreNeighbourhood::of;
					case "VonNeumann" -> VonNeumannNeighbourhood::of;
					default -> throw new IllegalArgumentException("Invalid neighbourhood in configuration");
				};
	}

	/**
	 * Constructor using default width
	 * @param environment the environment
	 * @param numExits the number of exits
	 * @param simulationConf the parameters used by the simulator
	 */
	public ExitEvacuationProblem(Environment environment, int numExits, SimulationConfiguration simulationConf) {
		this(environment, numExits, DEFAULT_EXIT_WIDTH, simulationConf);
	}

	/**
	 * Returns the environment
	 * @return the environment
	 */
	public Environment getEnvironment() {
		return environment;
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
	public double getHeight() {
		return environment.getDomain(1).getHeight();
	}


	public double getDiameter() {
		return diameter;
	}

	/**
	 * Simulates the evacuation given the list of exits are added
	 * to the environment.
	 * @param accesses list of exits to be added to the environment
	 * @return a summary of the simulation(s) performed
	 */
	public SimulationSummary simulate (List<Access> accesses) {
		var domainAccesses = domain.getAccesses();
		domainAccesses.addAll(accesses);

		// simulation metrics
		double nonEvacuees = 0.0;
		double minDistance = 0.0;
		double meanDistance = 0.0;
		double maxTime = 0.0;
		double meanTime = 0.0;
		double worseFitness = Double.NEGATIVE_INFINITY;

		// simulate

		// create common scenario for all simulations
		Scenario scenario = new Scenario.FromDomainBuilder(domain)
				.cellDimension(cellDimension)
				.floorField(floorField)
				.build();

		// create automaton for all simulations
		var cellularAutomatonParameters =
				new CellularAutomatonParameters.Builder()
						.scenario(scenario) // use this scenario
						.timeLimit(timeLimit) // time limit for simulation (in seconds)
						.neighbourhood(neighbourhood) // use this neighborhood for automaton
						.pedestrianReferenceVelocity(pedestrianReferenceVelocity) // fastest pedestrian speed
						.build();

		var automaton = new CellularAutomaton(cellularAutomatonParameters);
		// set a seed dependent on the solution for reproducibility
		es.uma.lcc.caesium.statistics.Random.random.setSeed(accesses.hashCode());
		// run numSimulations independent simulations
		for(int i = 0; i < numSimulations; i++) {
			// reset automaton for this simulation
			automaton.reset();

			// place pedestrians for this simulation
			Supplier<PedestrianParameters> pedestrianParametersSupplier = () ->
					new PedestrianParameters.Builder()
							.fieldAttractionBias(random.nextDouble(attractionBiasMin, attractionBiasMax))
							.crowdRepulsion(random.nextDouble(crowdRepulsionMin, crowdRepulsionMax))
							.velocityPercent(random.nextDouble(velocityFactorMin, velocityFactorMax))
							.build();

			var numberOfPedestrians = random.nextInt(numPedestriansMin, numPedestriansMax + 1);
			automaton.addPedestriansUniformly(numberOfPedestrians, pedestrianParametersSupplier);

			// run the simulation
			automaton.run();

			// gather metrics after this simulation
			double f = automaton.numberOfNonEvacuees();
			double curMinDist = 0.0;
			double curMeanDist = 0.0;
			double curMaxTime = 0.0;
			double curMeanTime = 0.0;
			if (f > 0) {
				var distances = automaton.distancesToClosestExit();
				curMinDist = minimum(distances);
				curMeanDist = mean(distances) * f;
			}
			else {
				var times = automaton.evacuationTimes();
				curMaxTime = maximum(times);
				curMeanTime = mean(times);
			}
			double curFitness = fitness (new SimulationSummary(f, curMinDist, curMeanDist, curMaxTime, curMeanTime));
			if (curFitness > worseFitness) { // the worst-case is kept
				nonEvacuees = f;
				minDistance = curMinDist;
				meanDistance = curMeanDist;
				maxTime = curMaxTime;
				meanTime = curMeanTime;
				worseFitness = curFitness;
			}
			
//			if (f > 0) {
//				nonEvacuees += f;
//				var distances = automaton.distancesToClosestExit();
//				minDistance += minimum(distances);
//				meanDistance += mean(distances) * f;
//			} else {
//				var times = automaton.evacuationTimes();
//				maxTime += maximum(times);
//				meanTime += mean(times);
//			}
		}

		domainAccesses.clear();
		domainAccesses.addAll(fixedAccesses);
		
// 		average results after all simulations
//		meanDistance /= nonEvacuees;
//		nonEvacuees /= numSimulations;
//		minDistance /= numSimulations;
//		maxTime /= numSimulations;
//		meanTime /= numSimulations;

		return new SimulationSummary(nonEvacuees, minDistance, meanDistance, maxTime, meanTime);
	}

	/**
	 * Computes fitness given the results of the simulation(s)
	 * @param summary summary of the simulation results
	 * @return a numeric value (to be minimized) representing the goodness of the simulation results.
	 */
	public double fitness(SimulationSummary summary) {
		double f = summary.nonEvacuees();
		if (f > 0) {
			f += summary.minDistance() / diameter + summary.meanDistance() / Math.pow(diameter, 2);
		} else {
			f += summary.maxTime() / timeLimit + summary.meanTime() / Math.pow(timeLimit, 2);
		}
		return f;
	}
	
	@Override
	public String toString() {
		return "================================================\nEvacuation Problem\n================================================"
				+ "\nEnvironment:     " + environment.jsonPrettyPrinted()
				+ "\nNumber of exits: " + numExits
				+ "\nExit width:      " + exitWidth 
				+ ((simulationConf == null) ? "\nSimulation configuration: TBD" : ("\n" + simulationConf)) 
				+ "\n================================================";
	}
}
