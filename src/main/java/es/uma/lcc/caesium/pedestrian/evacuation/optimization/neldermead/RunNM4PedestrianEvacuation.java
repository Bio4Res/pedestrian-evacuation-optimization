package es.uma.lcc.caesium.pedestrian.evacuation.optimization.neldermead;


import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.dfopt.base.IteratedDerivativeFreeMethod;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMead;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMeadConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;



/**
 * Resolution of the pedestrian evacuation problem using Nelder-Mead algorithm
 * @author ccottap
 * @version 1.0
 */
public class RunNM4PedestrianEvacuation {
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
			NelderMeadConfiguration conf = new NelderMeadConfiguration(jo);
			NelderMead solver = new NelderMead(conf);
			IteratedDerivativeFreeMethod myNM = new IteratedDerivativeFreeMethod(conf, solver);
			myNM.setVerbosityLevel(1);
			
			
		    Environment environment = Environment.fromFile(ENVIRONMENT_FILENAME + args[1] + ".json");
			SimulationConfiguration simulationConf = SimulationConfiguration.fromFile(args[3]);
		    int numExits = Integer.parseInt(args[2]);
		    ExitEvacuationProblem eep = new ExitEvacuationProblem (environment, numExits, simulationConf);

			DerivativeFreeEvacuationProblem nmep = new DerivativeFreeEvacuationProblem(eep);
			myNM.setObjectiveFunction(nmep);
					    
			for (int i=0; i<conf.getNumruns(); i++) {
				myNM.run();
				System.out.println ("Run " + i + ": " + 
						String.format("%.2f", myNM.getStatistics().getTime(i)) + "s\t" +
						myNM.getStatistics().getBest(i).value());
			}
			PrintWriter file = new PrintWriter(args[0] + STATS_FILENAME + args[1] + "-" + args[2] + ".json");
			file.print(myNM.getStatistics().toJSON().toJson());
			file.close();
			
			// Analyze the best solutions more in depth
			PrintWriter solsim = new PrintWriter(args[0] + SIMULATIONS_FILENAME + args[1] + "-" + args[2] + ".csv");
			PrintWriter sols = new PrintWriter(args[0] + SOLUTIONS_FILENAME + args[1] + "-" + args[2] + ".txt");

			final int NUMSIMS = 1000;
			solsim.print("run");
			for (int j=0; j<NUMSIMS; j++)
				solsim.print(",sim" + j);
			solsim.println();
			for (int i=0; i<conf.getNumruns(); i++) {
				solsim.print(i);
				var sol = myNM.getStatistics().getBest(i).point();
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
