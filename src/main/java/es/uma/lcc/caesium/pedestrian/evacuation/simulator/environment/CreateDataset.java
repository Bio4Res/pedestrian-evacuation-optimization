package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.util.JsonUtil;


/**
 * Class for generating a random dataset of instances
 * @author ccottap
 * @version 1.0
 */
public class CreateDataset {
	/**
	 * This RNG is used to sample parameter values
	 */
	private static Random rng = new Random(1); 

	/**
	 * Main mathod
	 * @param args command-line arguments (json configuration file)
	 * @throws JsonException if the configuration file is ill-formatted
	 * @throws FileNotFoundException if the configuration file cannot be read
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		String configurationFilename = ((args.length > 0) ? args[0] : "dataset.json");	
		
		// set US locale
		Locale.setDefault(Locale.US);

		// reads configuration
		JsonObject conf = (JsonObject) Jsoner.deserialize(new FileReader(configurationFilename));

		// extracts parameters
		String basename = ((conf.containsKey("basename")) ? (String) conf.get("basename") : "environment");
		int numInstances = ((conf.containsKey("instances")) ? JsonUtil.getInt(conf, "instances") : 1);
		long seed = ((conf.containsKey("seed")) ? JsonUtil.getLong(conf, "seed") : 1);		
		List<Double> widthRange = getDoubleInterval (conf, "width");
		List<Double> heightRange = getDoubleInterval (conf, "height");
		double cellDimension = ((conf.containsKey("cellDimension")) ? JsonUtil.getDouble(conf, "cellDimension") : 0.5);
		List<Integer> numberOfAccesses = getIntegerInterval (conf, "accesses");
		double accessWidth = ((conf.containsKey("accessWidth")) ? JsonUtil.getDouble(conf, "accessWidth") : 2.5);
		List<Integer> numberOfObstacles = getIntegerInterval (conf, "obstacles");
		
		// generate instances
		PrintWriter datasetStats = new PrintWriter(basename + "-stats.csv");
		datasetStats.println("width,height,area,obstacles,blocked,ratio");
		for (int i=0; i<numInstances; i++) {
			PrintWriter instanceFile = new PrintWriter(basename + "-" + (i+1) + ".json");
			var parameters = new RandomEnvironmentParameters.Builder()
			        .seed(seed + i) // use this seed
			        .width(sampleDouble(widthRange)) // width of the domain
			        .height(sampleDouble(heightRange)) // height of the domain
			        .cellDimension(cellDimension) // dimension of the cells (assumed to be square)
			        .numberOfObstacles(sampleInteger(numberOfObstacles)) // tentative number of obstacles to try to place in the domain
			        .numberOfAccesses(sampleInteger(numberOfAccesses)) // tentative number of accesses to try to place in the perimeter of the domain
			        .accessesWidth(accessWidth) // width of each of access
			        .build();

			var environment = new RandomEnvironment(parameters);
			analyze(datasetStats, environment);
			instanceFile.println(environment.jsonPrettyPrinted());
			instanceFile.close();
		}
		datasetStats.close();
	}
	
	/**
	 * Prints some stats about the environment
	 * @param datasetStats file to which the stats are printed
	 * @param environment an environment
	 */
	private static void analyze(PrintWriter datasetStats, RandomEnvironment environment) {
		Domain d = environment.getDomain(1);
		double w = d.getWidth();
		double h = d.getHeight();
		double area = w*h;
		double blocked = 0.0;
		List<Obstacle> obstacles = d.getObstacles();
		for (Obstacle o: obstacles) {
			Rectangle2D r = o.getShape().getAWTShape().getBounds2D();
			blocked += r.getWidth()*r.getHeight(); 
		}
		
		datasetStats.println(w + "," + h + "," + area + "," + obstacles.size() + "," + blocked + "," + blocked/area);
	}

	/**
	 * Gets an interval of double values from a json object
	 * @param json a json object
	 * @param key the key in which the interval is stored
	 * @return a list with two elements representing the lower end and the upper end of the interval
	 */
	private static List<Double> getDoubleInterval (JsonObject json, String key) {
		var interval = new ArrayList<Double>(2);
		JsonArray array = (JsonArray) json.get(key);
		interval.add(array.getDouble(0));
		interval.add(array.getDouble(1));
		return interval;
	}
	
	/**
	 * Gets an interval of integer values from a json object
	 * @param json a json object
	 * @param key the key in which the interval is stored
	 * @return a list with two elements representing the lower end and the upper end of the interval
	 */
	private static List<Integer> getIntegerInterval (JsonObject json, String key) {
		var interval = new ArrayList<Integer>(2);
		JsonArray array = (JsonArray) json.get(key);
		interval.add(array.getInteger(0));
		interval.add(array.getInteger(1));
		return interval;
	}
	
	/**
	 * Samples a double value from an interval 
	 * @param interval the interval [L, U)
	 * @return a double in range [L, U). If L == U, L is returned.
	 */
	private static double sampleDouble (List<Double> interval) {
		double l = interval.get(0);
		double u = interval.get(1);
		if (l == u)
			return l;
		else
			return rng.nextDouble(l, u);
	}
	
	/**
	 * Samples an integer value from an interval 
	 * @param interval the interval [L, U)
	 * @return a double in range [L, U). If L == U, L is returned.
	 */
	private static int sampleInteger (List<Integer> interval) {
		int l = interval.get(0);
		int u = interval.get(1);
		if (l == u)
			return l;
		else
			return rng.nextInt(l, u);
	}
	

}
