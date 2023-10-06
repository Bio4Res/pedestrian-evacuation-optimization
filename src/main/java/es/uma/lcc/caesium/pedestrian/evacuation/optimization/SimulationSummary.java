package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

/**
 * Record with some main descriptors of a simulation results (of their average for 
 * a certain number of simulations)
 * 
 * @param nonEvacuees (average) number of pedestrians who did not make it to the outside
 * @param minDistance (average) minimum distance of a non-evacuee to their nearest exit
 * @param meanDistance (average) mean distance of non-evacuees to their nearest exit
 * @param maxTime (average) time at which the last evacuee reached the exit
 * @param meanTime (average) mean time at which evacuees reached the exit
 */
public record SimulationSummary(double nonEvacuees, double minDistance, double meanDistance, double maxTime, double meanTime) {

}
