package es.uma.lcc.caesium.pedestrian.evacuation.environment;

/**
 * Parameters for a random environment with a single domain.
 *
 * @param seed              seed for random generator
 * @param width             width of the domain
 * @param height            height of the domain
 * @param cellDimension     dimension of the cells (assumed to be square)
 * @param numberOfObstacles tentative number of obstacles to try to place in the domain
 * @param numberOfAccesses  tentative number of accesses to try to place in the perimeter of the domain
 * @param accessesWidth     width of each of access
 * @author ppgllrd
 * @version 1.0
 */
public record RandomEnvironmentParameters(
    long seed, double width, double height, double cellDimension,
    int numberOfObstacles, int numberOfAccesses, double accessesWidth
) {
  // Private constructor with builder as parameter
  private RandomEnvironmentParameters(Builder builder) {
    this(builder.seed, builder.width, builder.height, builder.cellDimension,
        builder.numberOfObstacles, builder.numberOfAccesses, builder.accessesWidth);
  }

  // Public final builder class with empty constructor and static method for the first required step
  public static final class Builder {
    // Private fields for all parameters
    private long seed;
    private double width;
    private double height;
    private double cellDimension;
    private int numberOfObstacles;
    private int numberOfAccesses;
    private double accessesWidth;

    // Public constructor
    public Builder() {
    }

    /**
     * Sets the seed for the random generator
     *
     * @param seed seed for the random generator
     * @return a builder with the seed set
     */
    public SeedStep seed(long seed) {
      return new SeedStep(seed);
    }
  }

  // Non-nested class for the second required step
  public static final class SeedStep {
    // Private attribute of type Builder
    private final Builder builder;

    // Private constructor with seed as parameter
    private SeedStep(long seed) {
      this.builder = new Builder();
      this.builder.seed = seed; // set the first parameter
    }

    /**
     * Sets the width of the domain
     *
     * @param width width of the domain
     * @return a builder with the width set
     */
    public WidthStep width(double width) {
      this.builder.width = width; // set the second parameter
      return new WidthStep(this.builder);
    }
  }

  // Non-nested class for the third required step
  public static final class WidthStep {
    // Private attribute of type Builder
    private final Builder builder;

    // Private constructor with Builder as parameter
    private WidthStep(Builder builder) {
      this.builder = builder;
    }

    /**
     * Sets the height of the domain
     *
     * @param height height of the domain
     * @return a builder with the height set
     */
    public HeightStep height(double height) {
      this.builder.height = height; // set the third parameter
      return new HeightStep(this.builder);
    }
  }

  // Non-nested class for the fourth required step
  public static final class HeightStep {
    // Private attribute of type Builder
    private final Builder builder;

    // Private constructor with Builder as parameter
    private HeightStep(Builder builder) {
      this.builder = builder;
    }

    /**
     * Sets the dimension of the cells
     *
     * @param cellDimension dimension of the cells
     * @return a builder with the cell dimension set
     */
    public CellDimensionStep cellDimension(double cellDimension) {
      this.builder.cellDimension = cellDimension; // set the fourth parameter
      return new CellDimensionStep(this.builder);
    }
  }

  // Non-nested class for the optional steps and build method
  public static final class CellDimensionStep {
    // Private attribute of type Builder
    private final Builder builder;

    // Private constructor with Builder as parameter
    private CellDimensionStep(Builder builder) {
      this.builder = builder;
    }

    /**
     * Sets the tentative number of obstacles
     *
     * @param numberOfObstacles tentative number of obstacles
     * @return a builder with the number of obstacles set
     */
    public CellDimensionStep numberOfObstacles(int numberOfObstacles) {
      this.builder.numberOfObstacles = numberOfObstacles;
      return this;
    }

    /**
     * Sets the tentative number of accesses
     *
     * @param numberOfAccesses tentative number of accesses
     * @return a builder with the number of accesses set
     */
    public CellDimensionStep numberOfAccesses(int numberOfAccesses) {
      this.builder.numberOfAccesses = numberOfAccesses;
      return this;
    }

    /**
     * Sets the width of each access
     *
     * @param accessesWidth width of each access
     * @return a builder with the width of the accesses set
     */
    public CellDimensionStep accessesWidth(double accessesWidth) {
      this.builder.accessesWidth = accessesWidth;
      return this;
    }

    // Method to build the record object using the private constructor
    public RandomEnvironmentParameters build() {
      return new RandomEnvironmentParameters(this.builder);
    }
  }
}
