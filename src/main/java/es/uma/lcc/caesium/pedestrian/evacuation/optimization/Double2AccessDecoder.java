package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Shape;

/**
 * Decodes a floating-point number in [0,1] to a perimetral access of an environment
 * @author ppgllrd, ccottap
 * @version 1.0
 */
public class Double2AccessDecoder {
	/** 
	 * Width of exits in meters
	 */
	private final double exitWidth;
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
	 * Creates the decoder for a given exit evacuation problem
	 */
	public Double2AccessDecoder(ExitEvacuationProblem eep) {
		perimeterLength = eep.getPerimeterLength();
		exitWidth = eep.getExitWidth();
		width = eep.getWidth();
		height = eep.getHeight();
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
	
	/**
	 * Return a list of accesses corresponding to a location along the perimeter (they can 
	 * be more than one because the access may run across a corner, and this would be considered as
	 * two adjacent accesses).
	 * @param location the base location (a value between 0 and perimeterLength-exitWidth)
	 * @param label a number to use in the access name
	 * @param baseID the id to give to the access (sequentially incremented if there are more than one).
	 * @return a list with the corresponding accesses.
	 */
	public List<Access> decodeAccess (double location, int label, int baseID) {
		List<Access> exits = new ArrayList<>();
		int id = baseID;
		int r = 0;
		for(var rectangle : locationToRectangles(location)) {
			var access = new Access(id, String.format("access %d-%d", label, r), "", rectangle);
			exits.add(access);
			r++;
			id++;
		}
		return exits;
	}

}
