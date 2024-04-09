package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ea.PerimetralExitOptimizationFunction;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;

/**
 * Class for running the extensive testing of solutions
 * @author ccottap, ppgllrd
 * @version 1.0
 */
public class RunTestAnalysis {
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
	 * @throws JsonException if any json file is not correctly formatted
	 * @throws IOException if there is an error reading data
	 */
	public static void main(String[] args) throws JsonException, IOException {
		// set US locale
		Locale.setDefault(Locale.US);

		EAConfiguration conf;
		if (args.length < 4) {
			System.out.println ("Required parameters: <configuration-name> <environment-name> <num-exits> <simulation-configuration>");
			System.out.println ("\nNote that: ");
			System.out.println ("\t- the EA configuration file will be sought as <configuration-name>.json,");
			System.out.println ("\t- the environment configuration file will be sought as " + ENVIRONMENT_FILENAME + "<environment-name>.json,");
			System.out.println ("\t- the statistics will be dumped to a file named <configuration-name>" + STATS_FILENAME + "<environment-name>-<num-exits>.json");
			System.exit(1);
		}
				
		// Configure the EA
		FileReader reader = new FileReader(args[0] + ".json");
		conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
		int numruns = conf.getNumRuns();
		long maxevals = conf.getIslandConfiguration(0).getMaxEvaluations();
		conf.setVariationFactory(new HybridVariationFactory());
		System.out.println(conf);
		
		// Configure the problem
	    Environment environment = Environment.fromFile(ENVIRONMENT_FILENAME + args[1] + ".json");
		SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
	    int numExits = Integer.parseInt(args[2]);
	    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits, simulationConf);
	    PerimetralExitOptimizationFunction peof = new PerimetralExitOptimizationFunction(eep);

		//System.out.println(eep);
		System.out.println(simulationConf);
		
		FileReader statsFile = new FileReader(args[0] + STATS_FILENAME + args[1] + "-" + args[2] + ".json");
		JsonArray stats = (JsonArray) Jsoner.deserialize(statsFile);
		statsFile.close();
		
		List<List<Double>> solutions = new ArrayList<List<Double>> (numruns);
		for (int i=0; i<numruns; i++) {
			JsonArray rundata = (JsonArray) ((JsonObject)(stats.get(i))).get("rundata");
			int numIslands = rundata.size();
			double best = Double.POSITIVE_INFINITY;
			int index = 0;
			int row = 0;
			for (int j=0; j<numIslands; j++) {
				JsonObject isols = (JsonObject) ((JsonObject) rundata.get(j)).get("isols");
				JsonArray evals = (JsonArray) isols.get("evals");
				JsonArray fitnesses = (JsonArray) isols.get("fitness");
				for (int k=evals.size()-1; k>=0; k--) {
					long ev = evals.getLong(k);
					if (ev <= maxevals) {
						double fitness = fitnesses.getDouble(k);
						if (fitness < best) {
							best = fitness;
							index = j;
							row = k;
						}
						break;
					}
				}
			}
			JsonObject isols = (JsonObject) ((JsonObject) rundata.get(index)).get("isols");
			JsonArray evals = (JsonArray) isols.get("evals");
			JsonArray genomes = (JsonArray) isols.get("genome");
			JsonArray fitnesses = (JsonArray) isols.get("fitness");
			System.out.println("Run #" + i + ": best solution from island #" + index + " at evals=" + evals.getLong(row) + " with fitness=" + fitnesses.getDouble(row));
			JsonArray genome = (JsonArray) genomes.get(row);
			List<Double> sol = new ArrayList<Double>(numExits);
			for (int l=0; l<numExits; l++) {
				sol.add(genome.getDouble(l));
			}
			System.out.println(sol);
			solutions.add(sol);
		}

		// Analyze the best solutions more in depth
		PrintWriter solsim = new PrintWriter(args[0] + SIMULATIONS_FILENAME + args[1] + "-" + args[2] + ".csv");
		final int NUMSIMS = 1000;
		solsim.print("run");
		for (int j=0; j<NUMSIMS; j++)
			solsim.print(",sim" + j);
		solsim.println();
		for (int i=0; i<numruns; i++) {
			solsim.print(i);
			Individual ind = new Individual();
			Genotype g = new Genotype(numExits);
			List<Double> sol = solutions.get(i);
			for (int j=0; j<numExits; j++) {
				g.setGene(j, sol.get(j));
			}
			ind.setGenome(g);
			var summaries = eep.simulate(peof.decode(ind), NUMSIMS); 
			for (int j=0; j<NUMSIMS; j++)
				solsim.print("," + eep.fitness(summaries.get(j)));
			solsim.println();
		}
		solsim.close();
		
	}
}
