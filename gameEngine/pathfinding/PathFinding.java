package pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.Location;

/**
 * This class offers a generic way to perform path finding. It has implemented
 * an A* version of path finding and provides a few methods of heuristic
 * calculation and 'exploring' algorithms through the use of enums and abstract
 * classes.
 * <p>
 * I have really designed this class with extensibility in mind, so that new
 * methods of heuristics and exploring new tiles can be easily added without
 * duplicating code etc. See {@link Heuristic} and {@link Expansion} for
 * details.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class PathFinding {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(PathFinding.class);

	// ----------------------------------------------------------------
	// Heuristic enum types
	// ----------------------------------------------------------------
	/**
	 * A collection of heuristic methods which will be useful within A* path
	 * finding. A heuristics is the estimated distance between the current
	 * location and the destination location.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 * 
	 */
	public enum Heuristic {
		EUCLIDIAN_DISTANCE {
			@Override
			public double calculateHeuristic(Location current, Location goal) {
				return Helpers.calculateEuclideanDistance(current.getX(), current.getY(), goal.getX(), goal.getY());
			}
		},
		DISTANCE_SQUARED {
			@Override
			public double calculateHeuristic(Location current, Location goal) {
				return Helpers.calculateDistanceSquared(current.getX(), current.getY(), goal.getX(), goal.getY());
			}
		},
		MANHATTAN_DISTANCE {
			@Override
			public double calculateHeuristic(Location current, Location goal) {
				return Math.abs(current.getX() - goal.getX()) + Math.abs(current.getY() - goal.getY());
			}
		};

		public abstract double calculateHeuristic(Location current, Location goal);
	}

	// ----------------------------------------------------------------
	// 'Expansion' enum types
	// ----------------------------------------------------------------
	/**
	 * A list of possible 'expansion' methods, this is called by the A* path
	 * finding when trying to find the neighbouring cells. It is here that the
	 * limits of motion should be applied, for instance only being able move 4
	 * directions, or 8 directions etc.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 * 
	 */
	public enum Expansion {
		/**
		 * This algorithm returns the locations of 8 square movement, IE all
		 * sides.
		 */
		EIGHT_SQUARE {
			@Override
			public List<Location> getNeighbours(Location currentLocation, ITile[][] map) {
				List<Location> neighbours = new ArrayList<Location>();
				for (int y = currentLocation.getY() - 1, relativeY = 0; relativeY < 3; y++, relativeY++) {
					for (int x = currentLocation.getX() - 1, relativeX = 0; relativeX < 3; x++, relativeX++) {
						if (!(relativeX == 1 && relativeY == 1) && validTile(map, x, y)) {
							neighbours.add(new Location(x, y));
						}
					}
				}
				return neighbours;
			}
		},

		/**
		 * This algorithm only returns the locations of 4 tiles if they are
		 * valid. IE up, down, left, right
		 */
		FOUR_SQUARE {
			@Override
			public List<Location> getNeighbours(Location currentLocation, ITile[][] map) {
				List<Location> neighbours = new ArrayList<Location>();
				int y = currentLocation.getY();
				int x = currentLocation.getX();

				// Check left tile
				if (validTile(map, x - 1, y))
					neighbours.add(new Location(x - 1, y));
				// Check right tile
				if (validTile(map, x + 1, y))
					neighbours.add(new Location(x + 1, y));
				// check above tile
				if (validTile(map, x, y - 1))
					neighbours.add(new Location(x, y - 1));
				// check below tile
				if (validTile(map, x, y + 1))
					neighbours.add(new Location(x, y + 1));

				return neighbours;
			}
		};

		/**
		 * A static way to check if a tile is valid. IE if it exists within the
		 * bounds of the map and is passable.
		 * 
		 * @param map
		 *            The tiled map that we can explore
		 * @param x
		 *            The X tile we are performing the check on
		 * @param y
		 *            the Y tile we are performing the check on
		 * @return True if the tile is within the bounds of the map and is
		 *         passable. False if it isn't within the bounds of the map and
		 *         isn't passable.
		 */
		private static boolean validTile(ITile[][] map, int x, int y) {
			return (x >= 0 && x < map.length) && (y >= 0 && y < map[0].length) && map[x][y].isPassable();
		}

		public abstract List<Location> getNeighbours(Location currentLocation, ITile[][] map);
	}

	// ----------------------------------------------------------------
	// Breaking Ties enum types
	// ----------------------------------------------------------------
	/**
	 * Takes euclidian distance, then changes the heuristic a bit to reduce the
	 * total amount of visited tiles
	 * 
	 * Smoother heuristic from ::
	 * http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html
	 */
	public enum TieBreaking {
		BREAK_TIES {
			@Override
			public double calculateNewHeuristic(double heuristic, Location start, Location current, Location goal) {
				int dx1 = current.getX() - goal.getX();
				int dy1 = current.getY() - goal.getY();
				int dx2 = start.getX() - goal.getX();
				int dy2 = start.getY() - goal.getY();
				int cross = Math.abs(dx1 * dy2 - dx2 * dy1);

				return heuristic += cross * 0.001d;
			}
		},
		NONE {
			@Override
			public double calculateNewHeuristic(double heuristic, Location start, Location current, Location goal) {
				return heuristic;
			}
		};
		public abstract double calculateNewHeuristic(double heuristic, Location start, Location current, Location goal);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------

	/**
	 * Find the best path with the required algorithms for heuristic, expansion
	 * and tie breaking.
	 * 
	 * @param x1
	 *            The starting X tile
	 * @param y1
	 *            The starting Y tile
	 * @param x2
	 *            The ending X tile
	 * @param y2
	 *            The ending Y tile
	 * @param map
	 *            The map which we will explore
	 * @param heuristic
	 *            The herustic enum algorithm that we wish to apply
	 * @param tieBreaking
	 *            The tie breaking algorihm that we wish to apply
	 * @param expansion
	 *            The expansion algorithm that we wish to apply
	 * @return The path as an array of locations
	 */
	public static ArrayList<Location> findPath(int x1, int y1, int x2, int y2, ITile[][] map,
			Heuristic heuristic, TieBreaking tieBreaking, Expansion expansion) {
		AStarContainer result = findPath(new SearchNode(new Location(x1, y1)),
				new SearchNode(new Location(x2, y2)),
				map,
				heuristic, tieBreaking, expansion);
		return getAsLocationArray(result.endTile);
	}

	/**
	 * 
	 * @param searchNode
	 *            The end SearchNode
	 * @return An arraylist of tiles which form the best path
	 */
	private static ArrayList<Location> getAsLocationArray(SearchNode searchNode) {
		ArrayList<Location> searchPath = new ArrayList<Location>();
		SearchNode currentTile = searchNode;
		while ((currentTile = currentTile.parent) != null) {
			searchPath.add(currentTile.getAsLocation());
		}
		return searchPath;
	}

	/**
	 * Performs the A* path finding to the goal required.
	 * 
	 * @param start
	 *            The start SearchNode, this will be closed automatically when
	 *            searcing begins
	 * @param goal
	 *            The goal tile that we are searching for. When we reach this we
	 *            will end the search and return the path
	 * @param map
	 *            The map we are exploring
	 * @param heuristic
	 *            The enum type of the heuristic which we will call for
	 *            calculating the heuristic value
	 * @param tieBreaking
	 *            Any additional tie breaking
	 * @param expansion
	 *            The expansion algorithm enum which we will call when getting
	 *            the neighbour tiles of the current tile
	 * @return The best path. This 'AStarContainer' contains a lot more
	 *         information than that required to get the best path, but I have
	 *         done this for the ease of debugging.
	 */
	private static AStarContainer findPath(SearchNode start, SearchNode goal, ITile[][] map,
			Heuristic heuristic, TieBreaking tieBreaking, Expansion expansion) {
		// Priority Queue for getting the best tile back from path finding
		PriorityQueue<SearchNode> openTiles = new PriorityQueue<SearchNode>();
		List<SearchNode> closedTiles = new ArrayList<SearchNode>();

		Location startLocation = start.getAsLocation();
		Location goalLocation = goal.getAsLocation();

		openTiles.offer(start);

		// Each node is given a 'visitOrder' number, which will allow for us to
		// easily see what order a tile was visited in
		int visitOrder = 0;
		while (!openTiles.isEmpty()) {
			// Get the node with the lowest F, and remove it from openTiles
			SearchNode curTile = openTiles.poll();

			// If we've reached our goal, return all of the information about
			// this path find, which can be used to track the best path from
			// getting every parent's parent of curTile, and for debugging.
			if (curTile.equals(goal)) {
				return new AStarContainer(curTile, closedTiles, new ArrayList<SearchNode>(openTiles));
			}

			// Close the current tile so we don't navigate back to it
			closedTiles.add(curTile);

			// Set the visit order of this node, this will be used to see the
			// order of movement if debugging is required
			curTile.visitOrder = visitOrder++;

			// Get the neighbours and add it to our list
			List<Location> neighbourLocations = expansion.getNeighbours(curTile.getAsLocation(), map);
			for (Location neighbourLocation : neighbourLocations) {
				int x = neighbourLocation.getX();
				int y = neighbourLocation.getY();

				// Work out the step value cost. IE diagonals have a cost of 1.41,
				// horizontal and vertical have a cost of 1.0
				int diffX = curTile.getX() - x;
				int diffY = curTile.getY() - y;

				double stepCost = Math.abs(diffX) == Math.abs(diffY) ? 1.413d : 1d;

				double G = curTile.costG + stepCost + map[x][y].getTerrainCost();
				double H = heuristic.calculateHeuristic(neighbourLocation, goalLocation);

				// Apply any tie breaking
				H = tieBreaking.calculateNewHeuristic(H, startLocation, neighbourLocation, goalLocation);
				double F = G + H;

				SearchNode neighbourTile = new SearchNode(neighbourLocation,
						G,
						H,
						F,
						curTile);

				// Don't add a tile that we've already marked as closed, or
				// marked as possibly being explorable
				if (!closedTiles.contains(neighbourTile) && !openTiles.contains(neighbourTile)) {
					openTiles.offer(neighbourTile);
				}
			}
		}

		logger.error("Path not found for :: ", start, goal);
		// Do not return null, as we do not wish to crash whatever was relying
		// on the path to be foudn. Instead return a blank path
		return new AStarContainer(start, new ArrayList<SearchNode>(), new ArrayList<SearchNode>());
	}

	// ----------------------------------------------------------------
	// CREATED FOR DEBUGGING
	// ----------------------------------------------------------------
	/**
	 * This method has been created for debugging as it returns a lot more
	 * information than what would be expected from a normal path find.
	 * 
	 * @param start
	 *            The start SearchNode, this will be closed automatically when
	 *            searcing begins
	 * @param goal
	 *            The goal tile that we are searching for. When we reach this we
	 *            will end the search and return the path
	 * @param map
	 *            The map we are exploring
	 * @param heuristic
	 *            The enum type of the heuristic which we will call for
	 *            calculating the heuristic value
	 * @param tieBreaking
	 *            Any additional tie breaking
	 * @param expansion
	 *            The expansion algorithm enum which we will call when getting
	 *            the neighbour tiles of the current tile
	 * @return The best path. This 'AStarContainer' contains a lot more
	 *         information than that required to get the best path, but I have
	 *         done this for the ease of debugging
	 */
	public static AStarContainer findPathDebug(int x1, int y1, int x2, int y2,
			ITile[][] map,
			Heuristic heuristic, TieBreaking tieBreaking, Expansion expansion) {
		return findPath(new SearchNode(new Location(x1, y1)),
				new SearchNode(new Location(x2, y2)),
				map,
				heuristic, tieBreaking, expansion);
	}

	/**
	 * A basic static final inner class which we use to store the results of our
	 * path finding. This has been created for debugging, and is public for the
	 * same reasons.
	 */
	public static final class AStarContainer {
		public SearchNode endTile;
		public List<SearchNode> closedTiles;
		public List<SearchNode> openTiles;

		public AStarContainer(SearchNode endTile, List<SearchNode> closedTiles, List<SearchNode> openTiles) {
			this.endTile = endTile;
			this.closedTiles = closedTiles;
			this.openTiles = openTiles;
		}
	}

	// ----------------------------------------------------------------
	// private SearchNode inner class
	// ^ made public for debugging
	// ----------------------------------------------------------------
	public static final class SearchNode implements Comparable<SearchNode> {
		// ----------------------------------------------------------------
		// Fields
		// ----------------------------------------------------------------
		private Location location;
		private double costG, costH, costTotal;
		private SearchNode parent;

		// The number it was visited in. Gives us an indication of which order
		// tiles were visited in
		private int visitOrder;

		// ----------------------------------------------------------------
		// Constructors
		// ----------------------------------------------------------------
		SearchNode(Location location) {
			this.location = location;
		}

		SearchNode(Location location, double costG, double costH, double totalCost, SearchNode parent) {
			this(location);
			this.parent = parent;
			this.costG = costG;
			this.costH = costH;
			this.costTotal = totalCost;

			// Not visited
			this.visitOrder = -1;
		}

		// ----------------------------------------------------------------
		// Getters/Setters
		// ----------------------------------------------------------------
		public int getX() {
			return location.getX();
		}

		public int getY() {
			return location.getY();
		}

		public double getCostG() {
			return costG;
		}

		public double getCostH() {
			return costH;
		}

		public double getTotalCost() {
			return costTotal;
		}

		public SearchNode getParent() {
			return parent;
		}

		public Location getAsLocation() {
			return location;
		}

		public int getVisitOrder() {
			return visitOrder;
		}

		// ----------------------------------------------------------------
		// Misc
		// ----------------------------------------------------------------
		@Override
		public String toString() {
			return Helpers.concat("X :: ", getX(), " Y :: ", getY(), " Cost :: ", getTotalCost(), " Parent ::  ", getParent(), "\n");
		}

		@Override
		public boolean equals(Object foo) {
			if (foo instanceof SearchNode) {
				return getAsLocation().equals(((SearchNode) foo).getAsLocation());
			}
			return super.equals(foo);
		}

		@Override
		public int compareTo(SearchNode foo) {
			return Double.compare(costTotal, foo.getTotalCost());
		}
	}
}
