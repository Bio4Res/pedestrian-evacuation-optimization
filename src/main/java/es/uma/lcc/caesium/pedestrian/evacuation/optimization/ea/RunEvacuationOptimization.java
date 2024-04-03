package es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Class for running the evacuation optimization algorithm
 * @author ccottap, ppgllrd
 * @version 1.0
 */
public class RunEvacuationOptimization {
	/**
	 * environment filename prefix
	 */
	private static final String ENVIRONMENT_FILENAME = "base-";
	/**
	 * stats filename prefix
	 */
	private static final String STATS_FILENAME = "ea-stats-";

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws IOException if configuration file cannot be read
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws IOException, JsonException {
		// set US locale
		Locale.setDefault(Locale.US);

		EAConfiguration conf;
		if (args.length < 4) {
			System.out.println ("Required parameters: <ea-configuration-file> <environment-name> <num-exits> <simulation-configuration>");
			System.out.println ("\nNote that the environment configuration file will be sought as " + ENVIRONMENT_FILENAME + "<environment-name>.json,");
			System.out.println ("and the statistics will be dumped to a file named " + STATS_FILENAME + "<environment-name>.json");
			System.exit(1);
		}
		
		// Configure the EA
		FileReader reader = new FileReader(args[0]);
		conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
		int numruns = conf.getNumRuns();
		long firstSeed = conf.getSeed();
		System.out.println(conf);
		EvolutionaryAlgorithm myEA = new EvolutionaryAlgorithm(conf);
		myEA.setVerbosityLevel(1);
		
		// Configure the problem
	    Environment environment = Environment.fromFile(ENVIRONMENT_FILENAME + args[1] + ".json");
		SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
	    int numExits = Integer.parseInt(args[2]);
	    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits, simulationConf);
		myEA.setObjectiveFunction(new PerimetralExitOptimizationFunction(eep));
		myEA.getStatistics().setDiversityMeasure(new CircularSetDiversity(1.0));
		System.out.println(eep);
		
		for (int i=0; i<numruns; i++) {
			long seed = firstSeed + i;
			myEA.run(seed);
			System.out.println ("Run " + i + ": " + 
								String.format("%.2f", myEA.getStatistics().getTime(i)) + "s\t" +
								myEA.getStatistics().getBest(i).getFitness());
		}
		PrintWriter file = new PrintWriter(STATS_FILENAME + args[1] + ".json");
		file.print(myEA.getStatistics().toJSON().toJson());
		file.close();
	}
}
