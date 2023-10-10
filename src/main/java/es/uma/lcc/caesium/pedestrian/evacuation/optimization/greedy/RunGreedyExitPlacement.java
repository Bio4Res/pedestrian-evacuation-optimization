package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.ea.util.JsonUtil;
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
	 * stats filename
	 */
	private static final String STATS_FILENAME = "greedy_stats.csv";
	/**
	 * solution filename
	 */
	private static final String SOL_FILENAME = "greedy_solutions.csv";
	/**
	 * to decode locations
	 */
	private static Double2AccessDecoder decoder;
	/**
	 * the evacuation problem
	 */
	private static ExitEvacuationProblem eep;

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		if (args.length < 4) {
			System.out.println ("Required parameters: <greedy-configuration> <environment-configuration-file> <num-exits> <simulation-configuration>");
			System.exit(1);
		}
		
		// Configure the greedy resolution
		FileReader reader = new FileReader(args[0]);
		JsonObject json = (JsonObject) Jsoner.deserialize(reader);
		
		EAUtil.setSeed(JsonUtil.getLong(json, "seed"));
		int numruns = JsonUtil.getInt(json, "numruns");
		long maxevals = JsonUtil.getLong(json, "maxevals");
	
		
		// Configure the problem
	    Environment environment = Environment.fromFile(args[1]);
	    int numExits = Integer.parseInt(args[2]);
	    eep = new ExitEvacuationProblem (environment, numExits);
	    SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
	    eep.setSimulationConfiguration(simulationConf);
	    System.out.println(eep);
	    
	    decoder = new Double2AccessDecoder(eep);
    
	    GreedyPerimetralExitPlacement gpep = new GreedyPerimetralExitPlacement(eep);
	    gpep.setVerbosityLevel(0);
	    // this is the equivalent number of simulations required to compute a solution
	    int cost = (int)(Math.ceil(eep.getPerimeterLength()/eep.getExitWidth())*numExits);
	    
	    // Prepare the output files
	    PrintWriter stats = new PrintWriter (new File(STATS_FILENAME));
	    stats.println("run,evals,fitness");
	    PrintWriter sols = new PrintWriter (new File(SOL_FILENAME));
	    sols.print("run,evals");
	    for (int i=0; i<numExits; i++)
	    	sols.print(",exit" + i);
	    sols.println();
	    
	    for (int i=0; i<numruns; i++) {
	    	System.out.println("Run " + i);
	    	long evals = 0;
	    	double best = Double.POSITIVE_INFINITY;
	    	List<Double> bestsol = null;
	    	while (evals < maxevals) {
	    		List<Double> locations = gpep.getExits(numExits);
	    		List<Access> exits = decode(locations);
	    		double fitness = eep.fitness(eep.simulate(exits));
	    		evals += cost;
	    		if (fitness < best) {
	    			best = fitness;
	    			bestsol = locations;
	    			sols.print(i + "," + evals + "," + best);
	    		    for (int k=0; k<numExits; k++)
	    		    	sols.print("," + bestsol.get(k));
	    		    sols.println();
		    		System.out.println(i + "\t" + evals + "\t" + best);
	    		}
	    		stats.println(i + "," + evals + "," + best);
	    	}
	    }
	    stats.close();
	    sols.close();
	
	}

	/**
	 * Decodes a list of locations
	 * @param locations a its of locations in [0,1]
	 * @return a list of accesses
	 */
	private static List<Access> decode(List<Double> locations) {
		int numExits = locations.size();
		List<Access> exits = new ArrayList<>(numExits);
		var id = 0;
		for (int exit=0; exit<numExits; exit++) {
			double location = ((double)locations.get(exit))*(eep.getPerimeterLength()-eep.getExitWidth());
			exits.addAll(decoder.decodeAccess(location, exit, id));
			id = exits.size();
		}
		return exits;
	}
}
