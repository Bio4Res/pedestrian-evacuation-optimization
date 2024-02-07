package es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.util.JsonUtil;


/**
 * Configuration of the simulation
 * @author ccottap
 * @version 1.1
 */
public class SimulationConfiguration {
	/**
	 * map containing the parameter names and values
	 */
	private HashMap<String,String> parameters = new HashMap<String,String>();
	
	/**
	 * Returns the value of a key, which is assumed to be a double.
	 * @param key the name of the key
	 * @return the value of a key as a double.
	 */
	public double getDouble(String key) {
		return Double.parseDouble(parameters.get(key));
	}
	
	/**
	 * Returns the value of a key, which is assumed to be an integer.
	 * @param key the name of the key
	 * @return the value of a key as an integer.
	 */
	public int getInt(String key) {
		return (int)getDouble(key);
	}
	
	/**
	 * Returns the value of a key, which is assumed to be a string.
	 * @param key the name of the key
	 * @return the value of a key as a string.
	 */
	public String getString(String key) {
		return parameters.get(key);
	}
	
	/**
	 * Sets the value of a configuration property
	 * @param key name of the property
	 * @param value value of the property
	 */
	public void putValue(String key, String value) {
		parameters.put(key, value);
	}
	
	/** 
	 * Constructs a simulation configuration from a json object.
	 * @param json The json object to be parsed.
	 * @return the simulation configuration from the json object
	 */
	public static SimulationConfiguration fromJson(JsonObject json) {
		SimulationConfiguration conf = new SimulationConfiguration();
		int seed = 1;
		if (json.containsKey("seed")) 
			seed = JsonUtil.getInt(json, "seed");
		conf.parameters.put("seed", Integer.toString(seed));
		int numSimulations = 1;
		if (json.containsKey("numSimulations")) 
			numSimulations = JsonUtil.getInt(json, "numSimulations");
		conf.parameters.put("numSimulations", Integer.toString(numSimulations));
		
		JsonObject simulator = (JsonObject)json.get("simulator");
		conf.parameters.put("timeLimit", Double.toString(JsonUtil.getDouble(simulator,"timeLimit")));
		String type = (String)simulator.get("simulatorType");
		conf.parameters.put("simulatorType", type.toUpperCase());
		switch (type.toUpperCase()) {
		case "CA":
			JsonObject ca = (JsonObject)simulator.get("cellularAutomatonParameters");
			conf.parameters.put("cellularAutomatonParameters/cellDimension", Double.toString(JsonUtil.getDouble(ca, "cellDimension")));
			conf.parameters.put("cellularAutomatonParameters/neighborhood", (String)ca.get("neighborhood"));		
			conf.parameters.put("cellularAutomatonParameters/floorField", (String)ca.get("floorField"));	
			break;
		default:
			System.err.println("Configuration error: simulator type " + type + "unknown.");
			System.exit(1);
		}
		JsonObject crowd = (JsonObject)json.get("crowd");
		conf.parameters.put("crowd/pedestrianReferenceVelocity", Double.toString(JsonUtil.getDouble(crowd, "pedestrianReferenceVelocity")));
		String[] pedestrianKeys = {"numPedestrians", "attractionBias", "crowdRepulsion", "velocityFactor"};
		for (String key: pedestrianKeys) {
			putInterval(conf, (JsonArray) crowd.get(key), "crowd/" + key);
		}
		return conf;
	}

	/**
	 * Adds two parameters named "key/min" and "key/max" to the configuration. These are 
	 * read from the JsonArray provided (position[0] is min and position[1] is max)
	 * @param conf the configuration object
	 * @param pair the JsonArray with the interval
	 * @param key the basename of the key
	 */
	private static void putInterval(SimulationConfiguration conf, JsonArray pair, String key) {
		conf.parameters.put(key + "/" + "min", Double.toString(pair.getDouble(0)));
		conf.parameters.put(key + "/" + "max", Double.toString(pair.getDouble(1)));		
	}

	/**
	 * @param file the file with json contents to be parsed.
	 * @return the simulation configuration from the provided file.
	 * @throws FileNotFoundException if file is not found
	 * @throws JsonException if file does not contain a valid json object
	 */
	public static SimulationConfiguration fromFile(File file) throws FileNotFoundException, JsonException {
		FileReader reader = new FileReader(file);
		JsonObject json = (JsonObject) Jsoner.deserialize(reader);
		return fromJson(json);
	}

	/**
	 * @param filename the name of the file with json contents to be parsed.
	 * @return the simulation configuration from the provided file.
	 * @throws FileNotFoundException if file is not found
	 * @throws JsonException if file does not contain a valid json object
	 */
	public static SimulationConfiguration fromFile(String filename) throws FileNotFoundException, JsonException {
		return fromFile(new File(filename));
	}
	
	@Override
	public String toString() {
		String str = "------------------------------------------------\nSimulation configuration\n------------------------------------------------" 
				+ "\nseed:                          " + getInt("seed")
				+ "\nnumber of simulations:         " + getInt("numSimulations")
				+ "\ntime limit:                    " + getDouble("timeLimit");
		String type = getString("simulatorType");
		str += "\nsimulator type:                " + type;
		switch (type) {
		case "CA" :
			str += "\ncell dimension:                " + getDouble("cellularAutomatonParameters/cellDimension")
			     + "\nneighborhood:                  " + getString("cellularAutomatonParameters/neighborhood")
			     + "\nfloor field:                   " + getString("cellularAutomatonParameters/floorField");
			break;
		}
		str+= "\npedestrian reference velocity: " + getDouble("crowd/pedestrianReferenceVelocity")
		    + "\nnumber of pedestrians:         [" + getInt("crowd/numPedestrians/min")    + ", " +  getInt("crowd/numPedestrians/max")   + "]"
		    + "\nattraction bias:               [" + getDouble("crowd/attractionBias/min") + ", " + getDouble("crowd/attractionBias/max") + "]"
		    + "\ncrowd repulsion:               [" + getDouble("crowd/crowdRepulsion/min") + ", " + getDouble("crowd/crowdRepulsion/max") + "]"
		    + "\nvelocity factor:               [" + getDouble("crowd/velocityFactor/min") + ", " + getDouble("crowd/velocityFactor/max") + "]";
		str += "\n------------------------------------------------";
		return str;
	}
	
}
