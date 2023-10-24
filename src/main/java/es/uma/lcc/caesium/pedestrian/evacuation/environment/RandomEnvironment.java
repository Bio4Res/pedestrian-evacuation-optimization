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

  /**
   * Creates a random environment with a single domain.
   *
   * @param seed              seed for random generator
   * @param width             width of the domain
   * @param height            height of the domain
   * @param cellDimension     dimension of the cells (assumed to be square)
   * @param numberOfObstacles tentative number of obstacles to try to place in the domain
   * @param numberOfAccesses  tentative number of accesses to try to place in the perimeter of the domain
   * @param accessesWidth     width of each of access
   */
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
    int maxTries = numberOfAccesses * 100;
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
    maxTries = numberOfObstacles * 1000;
    while (numberOfBlocksPlaced < numberOfObstacles && maxTries > 0) {
      var obstacleWidth = // obstacle width as number of cells
          1 + (random.bernoulli(0.5) ?
               random.nextInt(2) :  // a vertical obstacle
               random.nextInt(25)); // an horizontal obstacle

      // obstacle height as number of cells.
      // less height the wider the obstacle. Never more than half of the rows in domain
      var obstacleHeight = 1 + random.nextInt(Math.max(1, rows / (2 * obstacleWidth)));

      // block position as number of cells
      var row = random.nextInt(0, 1 + rows - obstacleHeight);
      var column = random.nextInt(0, 1 + columns - obstacleWidth);

      // obstacle as a rectangle shape in domain coordinates
      var newObstacle = rectangleToShape(new Rectangle(row, column, obstacleHeight, obstacleWidth), cellDimension);

      // so that there is at least 2 cells between any two adjacent obstacles and accesses
      var border = rectangleToShape(new Rectangle(row - 2, column - 2, obstacleHeight + 4, obstacleWidth + 4),
          cellDimension);

      var shouldBePlaced = !intersectsAny(border, accesses)
          && !intersectsAny(border, obstacles);

      if (shouldBePlaced) {
        obstacles.add(newObstacle);
        numberOfBlocksPlaced++;
      }
      maxTries -= 1;
    }

    // Add accesses and obstacles to the domain
    int id = 1;
    for (var access : accesses) {
      domain.addAccess(new Access(id,
          String.format("access %d", id),
          String.format("access %d/%d", id, accesses.size()),
          access));
      id += 1;
    }

    id = 1;
    for (var obstacle : obstacles) {
      domain.addObstacle(new Obstacle(
          String.format("obstacle %d", id),
          String.format("obstacle %d/%d", id, obstacles.size()),
          obstacle));
      id += 1;
    }
  }

  /**
   * Creates a random environment with a single domain from specified parameters.
   *
   * @param parameters the parameters for the environment
   */
  public RandomEnvironment(RandomEnvironmentParameters parameters) {
    this(parameters.seed(), parameters.width(), parameters.height(), parameters.cellDimension(),
        parameters.numberOfObstacles(), parameters.numberOfAccesses(), parameters.accessesWidth());
  }

  /**
   * Converts a rectangle in cell coordinates to a rectangle in domain coordinates.
   *
   * @param rectangle     the rectangle in cell coordinates
   * @param cellDimension the dimension of a cell
   * @return the rectangle in domain coordinates
   */
  private static Shape.Rectangle rectangleToShape(Rectangle rectangle, double cellDimension) {
    return new Shape.Rectangle(rectangle.left() * cellDimension, rectangle.bottom() * cellDimension,
        rectangle.width() * cellDimension,
        rectangle.height() * cellDimension);
  }

  /**
   * Checks whether a rectangle intersects any of a set of rectangles.
   *
   * @param rectangle  the rectangle to check
   * @param rectangles the set of rectangles to check against
   * @return true if the rectangle intersects any of the rectangles in the set
   */
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

  /**
   * Checks whether any of a set of rectangles intersects any of another set of rectangles.
   *
   * @param these first set of rectangles
   * @param those second set of rectangles
   * @return true if any of the rectangles in the first set intersects any of the rectangles in the second set
   */
  private static boolean intersectsAny(Iterable<Shape.Rectangle> these, Iterable<Shape.Rectangle> those) {
    for (var rectangle : these) {
      if (intersectsAny(rectangle, those)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Function for testing the class.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    random.setSeed();
    var seed = random.nextLong();

    var parameters = new RandomEnvironmentParameters.Builder()
        .seed(seed) // use this seed
        .width(50) // width of the domain
        .height(25) // height of the domain
        .cellDimension(0.5) // dimension of the cells (assumed to be square)
        .numberOfObstacles(100) // tentative number of obstacles to try to place in the domain
        .numberOfAccesses(5) // tentative number of accesses to try to place in the perimeter of the domain
        .accessesWidth(2.5) // width of each of access
        .build();

    var environment = new RandomEnvironment(parameters);
    System.out.printf("Real number of obstacles is %d\n", environment.getNumberOfObstacles());

    // System.out.println(environment.jsonPrettyPrinted());

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
            .GUITimeFactor(10) // perform GUI animation x10 times faster than real time
            .build();

    var cellularAutomaton = new CellularAutomaton(cellularAutomatonParameters);

    // place pedestrians
    var numberOfPedestrians = random.nextInt(300, 800);
    Supplier<PedestrianParameters> pedestrianParametersSupplier = () ->
        new PedestrianParameters.Builder()
            .fieldAttractionBias(random.nextDouble(2.0, 4.0))
            .crowdRepulsion(random.nextDouble(1.00, 1.50))
            .velocityPercent(random.nextDouble(0.5, 1.0))
            .build();
    cellularAutomaton.addPedestriansUniformly(numberOfPedestrians, pedestrianParametersSupplier);

    cellularAutomaton.runGUI();
  }

  /**
   * Returns the only domain in this environment.
   *
   * @return the only domain in this environment.
   */
  public double getCellDimension() {
    return cellDimension;
  }

  /**
   * Returns the width of each access.
   *
   * @return the width of each access
   */
  public double getAccessesWidth() {
    return accessesWidth;
  }

  /**
   * Returns the number of obstacles really placed in this environment.
   *
   * @return the number of obstacles really placed in this environment.
   */
  public int getNumberOfObstacles() {
    return domain.getObstacles().size();
  }

  /**
   * Returns the number of accesses really placed in this environment.
   *
   * @return the number of accesses really placed in this environment.
   */
  public int getNumberOfAccesses() {
    return domain.getAccesses().size();
  }
}
