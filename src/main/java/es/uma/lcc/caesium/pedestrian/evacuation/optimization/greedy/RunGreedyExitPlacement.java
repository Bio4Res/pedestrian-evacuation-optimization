package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonException;

import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Class for running the greedy evacuation optimization algorithm
 * @author ccottap
 * @version 1.0
 */
public class RunGreedyExitPlacement {

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		if (args.length < 3) {
			System.out.println ("Required parameters: <environment-configuration-file> <num-exits> <simulation-configuration>");
			System.exit(1);
		}
		
		EAUtil.setSeed(1);
		
		// Configure the problem
	    Environment environment = Environment.fromFile(args[0]);
	    int numExits = Integer.parseInt(args[1]);
	    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits);
	    SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[2]);
	    eep.setSimulationConfiguration(simulationConf);
	    System.out.println(eep);
	    
	    Double2AccessDecoder decoder = new Double2AccessDecoder(eep);

	    
	    GreedyPerimetralExitPlacement gpep = new GreedyPerimetralExitPlacement(eep);
	    // this is the equivalent number of simulations required to compute a solution
	    double cost = (int)(Math.ceil(eep.getPerimeterLength()/eep.getExitWidth())*numExits);
	    
	    List<Double> locations = gpep.getExits(numExits);
	    System.out.println("Solution: " + locations);
	    for (int i=0; i<numExits; i++) {
	    	double pos = locations.get(i) * (eep.getPerimeterLength() - eep.getExitWidth());
	    	System.out.print("[" + pos + ", " + (pos + eep.getExitWidth()) + "]");
	    }
	    System.out.println();
	    
	    List<Access> acc = new ArrayList<Access>(numExits);
	    int id = 0;
	    for (double loc : locations) {
	    	acc.addAll(decoder.decodeAccess(loc* (eep.getPerimeterLength() - eep.getExitWidth()), id++, acc.size()));
	    }
	    System.out.println("Accesses: " + acc);
	    System.out.println("Fitness : " + eep.fitness(eep.simulate(acc)));

	    System.out.println("Cost    : " + cost + " simulations");
	
	}
}
