package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Shape;


/**
 * Objective function of the EA in order to find the location of a certain fixed
 * number of exits in the perimeter of a given environment, so that the evacuation 
 * performance is optimized.
 * @author ccottap, ppgllrd
 * @version 1.0
 *
 */
public class PerimetralExitOptimizationFunction extends ContinuousObjectiveFunction {
	/**
	 * Width of exits in meters
	 */
	private final double exitWidth;
	/**
	 * number of exits
	 */
	private final int numExits;
	/**
	 * length of the perimeter
	 */
	private final double perimeterLength;
	/**
	 * Width of the environment
	 */
	private final double width;
	/**
	 * height of the environment
	 */
	private final double height;
	/**
	 * the instance of the evacuation problem
	 */
	private final ExitEvacuationProblem eep;
	
	/**
	 * Basic constructor
	 * @param eep the evacuation problem
	 */
	public PerimetralExitOptimizationFunction(ExitEvacuationProblem eep) {
		super(eep.getNumExits(), 0.0, 1.0);
		numExits = eep.getNumExits();
		perimeterLength = eep.getPerimeterLength();
		exitWidth = eep.getExitWidth();
		width = eep.getWidth();
		height = eep.geHeight();
		this.eep = eep;
	}
	
	/**
	 * Indicates whether the goal is maximization or minimization
	 * @return the optimization sense
	 */
	public OptimizationSense getOptimizationSense()
	{
		return OptimizationSense.MINIMIZATION;
	}

	/*                                                       <---
	                                           right             |
  2w+h--------------------------------|--------|-----------w+h |
	   |                                 --------            |
	   |                                                     |
 top -                                                     -
	   ||                                                   ||
	   ||                                                   ||
	   ||                                                   ||
	   -                                                     - bottom
	   |                                                     |
	   |            -------                                  |   ^
	   0-----------|-------|---------------------------------w   |
                 left                                          |
     0 --->                                                ----

	 */

	/**
	 * Returns a list of rectangles corresponding to segments of an exit located
	 * at location with width `exitWidth`.
	 * @param location  location across boundaries of domain.
	 * @return a list of rectangles corresponding to segments of such exit.
	 */
	private List<Shape.Rectangle> locationToRectangles(double location) {
		var exitHeight = 0.1; // an exit is going to be represented as a rectangle. This is its height
		var remainingExitLength = exitWidth;
		List<Shape.Rectangle> rectangles = new LinkedList<>();
		while(remainingExitLength > 0) {
			while (location >= perimeterLength)
				location -= perimeterLength;
			if (location < width) {
				// horizontal. bottom. left to right
				var left = location;
				var rectangleWidth = Math.min(remainingExitLength, width - left);
				rectangles.add(new Shape.Rectangle(left, 0, rectangleWidth, exitHeight));
				location += rectangleWidth;
				remainingExitLength -= rectangleWidth;
			} else if (location < (width + height)) {
				// vertical. right. bottom to top
				var bottom = location - width;
				var rectangleHeight = Math.min(remainingExitLength, height - bottom);
				rectangles.add(new Shape.Rectangle(width - exitHeight, bottom, exitHeight, rectangleHeight));
				location += rectangleHeight;
				remainingExitLength -= rectangleHeight;
			} else if (location < (2 * width + height)) {
				// horizontal. top. right to left
				var right = width - (location - (width + height));
				var rectangleWidth = Math.min(remainingExitLength, right);
				rectangles.add(new Shape.Rectangle(right - rectangleWidth, height - exitHeight, rectangleWidth, exitHeight));
				location += rectangleWidth;
				remainingExitLength -= rectangleWidth;
			} else {
				// vertical. left. top to bottom
				var top = height - (location - (2 * width + height));
				var rectangleHeight = Math.min(remainingExitLength, top);
				rectangles.add(new Shape.Rectangle(0, top - rectangleHeight, exitHeight, rectangleHeight));
				location += rectangleHeight;
				remainingExitLength -= rectangleHeight;
			}
		}
		return rectangles;
	}

	@Override
	protected double _evaluate(Individual ind) {
		List<Access> exits = new ArrayList<>(numExits);
		Genotype g = ind.getGenome();
		var id = 0;
		for (int exit=0; exit<numExits; exit++) {
			double location = ((double)g.getGene(exit))*(perimeterLength-exitWidth);
			// create access and add to list
			// consider width and height to account for exits that are across a corner of the environment
			var r = 0;
			for(var rectangle : locationToRectangles(location)) {
				var access = new Access(id, String.format("access %d-%d", exit, r), "", rectangle);
				exits.add(access);
				r++;
				id++;
			}
		}
		// simulate
		return eep.simulate(exits);
	}
}
