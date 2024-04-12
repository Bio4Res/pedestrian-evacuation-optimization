package es.uma.lcc.caesium.pedestrian.evacuation.optimization.dfopt;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.dfopt.base.DerivativeFreeConfiguration;
import es.uma.lcc.caesium.dfopt.hookejeeves.HookeJeevesConfiguration;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMeadConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
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
	 * stats filename prefix
	 */
	private static final String SOLUTIONS_FILENAME = "-solutions-";
	/**
	 * solution simulation filename prefix
	 */
	private static final String SIMULATIONS_FILENAME = "-simulations-";

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws IOException if the input file cannot be read or the solution/randomly generated instance cannot be written
	 * @throws JsonException if configuration file cannot be read
	 */
	public static void main(String[] args) throws IOException, JsonException {

		if (args.length<4) {
			System.out.println ("Required parameters: <configuration-name> <environment-name> <num-exits> <simulation-configuration>");
			System.exit(-1);
		}
		else {
			FileReader reader = new FileReader(args[0] + ".json");
			JsonObject jo = (JsonObject) Jsoner.deserialize(reader);
			reader.close();

			DerivativeFreeConfiguration conf = null;

			if (args[0].toLowerCase().contains("neldermead")) {
				conf = new NelderMeadConfiguration(jo);
			}
			else if (args[0].toLowerCase().contains("hookejeeves")) {
				conf = new HookeJeevesConfiguration(jo);
			}
			else {
				System.out.println("Unknown method: " + args[0]);
				System.exit(1);
			}

			System.out.println(conf);
			int numruns = conf.getNumruns();
			long maxevals = conf.getMaxevals();

			// Configure the problem
			Environment environment = Environment.fromFile(ENVIRONMENT_FILENAME + args[1] + ".json");
			SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
			int numExits = Integer.parseInt(args[2]);
			ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits, simulationConf);

			DerivativeFreeEvacuationProblem nmep = new DerivativeFreeEvacuationProblem(eep);	   

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
				System.out.println("Run #" + i + ": best solution from #" + index + " at evals=" + evals.getLong(row) + " with fitness=" + fitnesses.getDouble(row));
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
			PrintWriter sols = new PrintWriter(args[0] + SOLUTIONS_FILENAME + args[1] + "-" + args[2] + ".txt");
			final int NUMSIMS = 1000;
			solsim.print("run");
			for (int j=0; j<NUMSIMS; j++)
				solsim.print(",sim" + j);
			solsim.println();
			for (int i=0; i<numruns; i++) {
				solsim.print(i);
				List<Double> sol = solutions.get(i);
				var summaries = eep.simulate(nmep.decode(sol), NUMSIMS); 
				for (int j=0; j<NUMSIMS; j++)
					solsim.print("," + eep.fitness(summaries.get(j)));
				solsim.println();
				for (int j=0; j<sol.size(); j++) {
					sols.print(sol.get(j) + "\t");
				}
				sols.println();
			}
			solsim.close();
			sols.close();
		}

	}
}
