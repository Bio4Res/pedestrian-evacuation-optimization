package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.io.FileReader;
import java.io.IOException;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.util.JsonUtil;

/**
 * Runs analysis in batch
 * @author ccottap
 * @version 1.0
 */
public class RunBatchAnalysis {

	/**
	 * Main method to run experiments in batch
	 * @param args commnad-line arguments: name of the batch file
	 * @throws IOException if the file (or any file referenced within it) cannot be found
	 * @throws JsonException if the file (or any file referenced within it) is not properly formatted
	 */
	public static void main(String[] args) throws IOException, JsonException {
		if (args.length < 1) {
			System.out.println("Missing parameters. Required: <batch-conf>");
			System.exit(1);
		}
		// reads batch configuration
		JsonObject conf = (JsonObject) Jsoner.deserialize(new FileReader(args[0]));
		String algorithmConf = (String) conf.get("algorithm");
		int numExits = JsonUtil.getInt(conf, "exits");
		String simulationConf = (String) conf.get("simulation");
		JsonArray experiments = (JsonArray) conf.get("experiments");

		String[] params = new String[4];
		params[0] = algorithmConf;
		params[2] = String.valueOf(numExits);
		params[3] = simulationConf;
		
		for (Object o: experiments) {
			JsonObject exp = (JsonObject)o;
			String environment = (String) exp.get("basename");
			int num = JsonUtil.getInt(exp, "num");
			int first = 1;
			if (exp.containsKey("start")) {
				first = JsonUtil.getInt(exp, "start");
			}

			for (int i=first; i<=num; i++) {
				params[1] = environment + "-" + String.valueOf(i);
				System.out.println("--------------------------------------------------------------------------------");
				System.out.println("Running analysis for " + params[0] + " " + params[1] + " " + params[2] + " " + params[3]);
				System.out.println("--------------------------------------------------------------------------------");
				RunTestAnalysis.main(params);
			}

		}

	}

}
