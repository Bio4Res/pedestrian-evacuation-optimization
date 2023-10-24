package es.uma.lcc.caesium.pedestrian.evacuation.environment;

import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.*;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.function.Supplier;

import static es.uma.lcc.caesium.statistics.Random.random;

/**
 * A class for generating random environments
 * <p>
 *
 * @author ppgllrd
 * @version 1.0
 */
public class RandomEnvironment extends Environment {

  private final double cellDimension;
  private final double accessesWidth;
  private final Domain domain;

  public RandomEnvironment(long seed, double width, double height, double cellDimension,
                           int numberOfObstacles, int numberOfAccesses, double accessesWidth) {
    super();

    random.setSeed(seed);

    int rows = (int) (height / cellDimension);
    int columns = (int) (width / cellDimension);
    this.cellDimension = cellDimension;
    this.accessesWidth = accessesWidth;

    domain = new Domain(1, width, height);
    this.addDomain(domain);

    var accesses = new ArrayList<Shape.Rectangle>();
    var obstacles = new ArrayList<Shape.Rectangle>();

    // try to place accesses
    var double2AccessDecoder = Double2AccessDecoder.forDomain(domain, accessesWidth);
    var perimeter = 2 * (rows + columns);
    int maxTries = numberOfAccesses * 10;
    int numberOfAccessesPlaced = 0;
    while (numberOfAccessesPlaced < numberOfAccesses && maxTries > 0) {
      var location = cellDimension * random.nextInt(0, perimeter);
      var newsAccesses = double2AccessDecoder.locationToRectangles(location);

      var shouldBePlaced = !intersectsAny(newsAccesses, accesses);

      if (shouldBePlaced) {
        accesses.addAll(newsAccesses);
        numberOfAccessesPlaced++;
      }
      maxTries -= 1;
    }

    // try to place obstacles
    int numberOfBlocksPlaced = 0;
    maxTries = numberOfObstacles * 10;
    while (numberOfBlocksPlaced < numberOfObstacles && maxTries > 0) {
      var blockWidth =
          1 + (random.bernoulli(0.5) ?
                  random.nextInt(2) :  // a vertical obstacle
                  random.nextInt(25)); // an horizontal obstacle

      // less height the wider the obstacle. Never more than half of the rows
      var blockHeight = 1 + random.nextInt(Math.max(1, rows / (2 * blockWidth)));

      var row = random.nextInt(0, 1 + rows - blockHeight);
      var column = random.nextInt(0, 1 + columns - blockWidth);

      var newBlock = rectangleToShape(new Rectangle(row, column, blockHeight, blockWidth), cellDimension);
      // so that obstacles are apart
      var border = rectangleToShape(new Rectangle(row - 2, column - 2, blockHeight + 4, blockWidth + 4), cellDimension);

      var shouldBePlaced = !intersectsAny(border, accesses)
          && !intersectsAny(border, obstacles);

      if (shouldBePlaced) {
        obstacles.add(newBlock);
        numberOfBlocksPlaced++;
      }
      maxTries -= 1;
    }

    // Add accesses and obstacles to the domain
    int id = 0;
    for (var access : accesses) {
      domain.addAccess(new Access(id,
          String.format("access %d", id),
          String.format("access %d/%d", id, accesses.size()),
          access));
      id += 1;
    }

    id = 0;
    for (var obstacle : obstacles) {
      domain.addObstacle(new Obstacle(
          String.format("obstacle %d", id),
          String.format("obstacle %d/%d", id, obstacles.size()),
          obstacle));
      id += 1;
    }
  }

  private static Shape.Rectangle rectangleToShape(Rectangle rectangle, double cellDimension) {
    return new Shape.Rectangle(rectangle.left() * cellDimension, rectangle.bottom() * cellDimension,
        rectangle.width() * cellDimension,
        rectangle.height() * cellDimension);
  }

  private static boolean intersectsAny(Shape.Rectangle rectangle, Iterable<Shape.Rectangle> rectangles) {
    var area = new Area(rectangle.getAWTShape());
    for (var other : rectangles) {
      var otherArea = new Area(other.getAWTShape());
      otherArea.intersect(area);
      if (!otherArea.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private static boolean intersectsAny(Iterable<Shape.Rectangle> these, Iterable<Shape.Rectangle> those) {
    for (var rectangle : these) {
      if (intersectsAny(rectangle, those)) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    random.setSeed();
    var seed = random.nextLong();

    var environment = new RandomEnvironment(seed, 50, 25, 0.5
        , 120, 5, 2.5);

    var scenario = new Scenario.FromDomainBuilder(environment.domain)
        .cellDimension(environment.cellDimension)
        .floorField(DijkstraStaticFloorFieldWithMooreNeighbourhood::of)
        .build();

    var cellularAutomatonParameters =
        new CellularAutomatonParameters.Builder()
            .scenario(scenario) // use this scenario
            .timeLimit(10 * 60) // time limit for simulation (in seconds)
            .neighbourhood(MooreNeighbourhood::of) // use this neighborhood for automaton
            .pedestrianReferenceVelocity(1.5) // fastest pedestrian speed
            .GUITimeFactor(8)
            .build();

    var cellularAutomaton = new CellularAutomaton(cellularAutomatonParameters);

    // place pedestrians
    var numberOfPedestrians = random.nextInt(150, 500);
    Supplier<PedestrianParameters> pedestrianParametersSupplier = () ->
        new PedestrianParameters.Builder()
            .fieldAttractionBias(random.nextDouble(2.0, 4.0))
            .crowdRepulsion(random.nextDouble(1.00, 1.50))
            .velocityPercent(random.nextDouble(0.5, 1.0))
            .build();
    cellularAutomaton.addPedestriansUniformly(numberOfPedestrians, pedestrianParametersSupplier);

    cellularAutomaton.runGUI();
  }

  public double getCellDimension() {
    return cellDimension;
  }

  public double getAccessesWidth() { return accessesWidth; }

  public int getNumberOfObstacles() {
    return domain.getObstacles().size();
  }

  public int getNumberOfAccesses() {
    return domain.getAccesses().size();
  }
}
