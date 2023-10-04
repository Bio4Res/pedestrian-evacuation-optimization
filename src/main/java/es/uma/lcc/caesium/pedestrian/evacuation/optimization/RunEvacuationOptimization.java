package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.ea.statistics.VarianceDiversity;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Class for running the evacuation optimization algorithm
 * @author ccottap
 * @version 1.0
 */
public class RunEvacuationOptimization {

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		EAConfiguration conf;
		if (args.length < 3) {
			System.out.println ("Required parameters: <ea-configuration-file> <environment-configuration-file> {<evacuation-configuration>}");
			System.exit(1);
		}
		// Configure the EA
		FileReader reader = new FileReader(args[0]);
		conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
		int numruns = conf.getNumRuns();
		long firstSeed = conf.getSeed();
		System.out.println(conf);
		EvolutionaryAlgorithm myEA = new EvolutionaryAlgorithm(conf);
		// Configure the problem
	    Environment environment = Environment.fromFile(args[1]);
	    int numExits = Integer.parseInt(args[2]);
	    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits);
	    System.out.println(eep);
		myEA.setObjectiveFunction(new PerimetralExitOptimizationFunction(eep));
		myEA.getStatistics().setDiversityMeasure(new VarianceDiversity());
		for (int i=0; i<numruns; i++) {
			long seed = firstSeed + i;
			// set seed for running simulator and EA
			es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.statistics.Random.random.setSeed(seed);
			myEA.run(seed); 
			System.out.println ("Run " + i + ": " + 
								String.format(Locale.US, "%.2f", myEA.getStatistics().getTime(i)) + "s\t" +
								myEA.getStatistics().getBest(i).getFitness());
		}
		PrintWriter file = new PrintWriter("stats.json");
		file.print(myEA.getStatistics().toJSON().toJson());
		file.close();
	}
}
