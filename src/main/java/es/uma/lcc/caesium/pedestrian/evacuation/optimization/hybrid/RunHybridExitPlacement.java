package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.CircularSetDiversity;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.PerimetralExitOptimizationFunction;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Class for running the greedy evacuation optimization algorithm
 * @author ccottap, ppgllrd
 * @version 1.0
 */
public class RunHybridExitPlacement {
	/**
	 * environment filename prefix
	 */
	private static final String ENVIRONMENT_FILENAME = "base-";
	/**
	 * stats filename prefix
	 */
	private static final String STATS_FILENAME = "-stats-";
	/**
	 * solution simulation filename prefix
	 */
	private static final String SIMULATIONS_FILENAME = "-simulations-";
	
	
	
	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		// set US locale
		Locale.setDefault(Locale.US);

		EAConfiguration conf;
		if (args.length < 4) {
			System.out.println ("Required parameters: <configuration-name> <environment-name> <num-exits> <simulation-configuration>");
			System.out.println ("\nNote that: ");
			System.out.println ("\t- the EA configuration file will be sought as <configuration-name>.json,");
			System.out.println ("\t- the environment configuration file will be sought as " + ENVIRONMENT_FILENAME + "<environment-name>.json,");
			System.out.println ("\t- the statistics will be dumped to a file named <configuration-name>" + STATS_FILENAME + "<environment-name>.json");
			System.exit(1);
		}
		
		// Configure the EA
		FileReader reader = new FileReader(args[0] + ".json");
		conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
		int numruns = conf.getNumRuns();
		long firstSeed = conf.getSeed();
		conf.setVariationFactory(new HybridVariationFactory());
		System.out.println(conf);
		EvolutionaryAlgorithm myEA = new EvolutionaryAlgorithm(conf);
		myEA.setVerbosityLevel(1);
		
		// Configure the problem
	    Environment environment = Environment.fromFile(ENVIRONMENT_FILENAME + args[1] + ".json");
		SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
	    int numExits = Integer.parseInt(args[2]);
	    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits, simulationConf);
	    PerimetralExitOptimizationFunction peof = new PerimetralExitOptimizationFunction(eep);
		myEA.setObjectiveFunction(peof);
		myEA.getStatistics().setDiversityMeasure(new CircularSetDiversity(1.0));
		System.out.println(eep);
		
		for (int i=0; i<numruns; i++) {
			long seed = firstSeed + i;
			myEA.run(seed);
			System.out.println ("Run " + i + ": " + 
								String.format("%.2f", myEA.getStatistics().getTime(i)) + "s\t" +
								myEA.getStatistics().getBest(i).getFitness());
		}
		PrintWriter file = new PrintWriter(args[0] + STATS_FILENAME + args[1] + ".json");
		file.print(myEA.getStatistics().toJSON().toJson());
		file.close();
		
		// Analyze the best solutions more in depth
		PrintWriter solsim = new PrintWriter(args[0] + SIMULATIONS_FILENAME + args[1] + ".csv");
		final int NUMSIMS = 1000;
		solsim.print("run");
		for (int j=0; j<NUMSIMS; j++)
			solsim.print(",sim" + j);
		solsim.println();
		for (int i=0; i<numruns; i++) {
			solsim.print(i);
			var summaries = eep.simulate(peof.decode(myEA.getStatistics().getBest(i)), NUMSIMS); 
			for (int j=0; j<NUMSIMS; j++)
				solsim.print("," + eep.fitness(summaries.get(j)));
			solsim.println();
		}
		solsim.close();
		
	}
}
