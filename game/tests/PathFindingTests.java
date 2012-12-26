package tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pathfinding.ITile;
import pathfinding.PathFinding;
import pathfinding.PathFinding.AStarContainer;
import pathfinding.PathFinding.SearchNode;

import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.Location;
import eventhandling.IKeyboardEventable;
import eventhandling.KeyEventHandler;

/**
 * This class shows the basic implementations of the a* path finding algorithm
 * which the game engine offers. In itself it is not a great example of how to
 * create a map that can be explored etc, but simply how to use the path finding
 * algorithm and the different results that it produces.
 * <p>
 * This example allows the user to click with their left mouse within the
 * gridded to define the starting location, and the right mouse to define the
 * goal location.
 * <p>
 * This example also allows you to select between the different heuristics
 * offered by the path finding system, IE, distance squared, Manhattan distance
 * etc.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class PathFindingTests extends GameScreen {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(PathFindingTests.class);

	GameLayer tiledWorld;

	public PathFindingTests() {
		super("pathFindingTests");

		tiledWorld = new TiledWorld();

		addGameLayer(tiledWorld);
	}

	/**
	 * Note this is just an example to show the A* path finding algorithm, and
	 * that the implementation of the actual tiles etc should be ignored... (as
	 * it's awful)
	 * <p>
	 * This is not a prime example of my programming, it's just for
	 * demonstrative/testing purposes
	 */
	private static final class TiledWorld extends GameLayer implements IKeyboardEventable {
		/**
		 * Access to the key event handler so that we can register ourselfs to
		 * recieve key events, which we use to change the path finding herustic
		 * wanted when characters 1-n are pressed
		 */
		private final static KeyEventHandler keyEventHandler = KeyEventHandler.getInstance();

		/**
		 * Stores information on which path finding method was chosen etc. This
		 * is drawn to the screen.
		 */
		private String debugString = "";
		/**
		 * Stores information on exactly how many tiles the path finder explored
		 * etc. This is drawn to the screen.
		 */
		private String foundPathInString = "";

		/**
		 * The 2d array of tiles, IE or explorable map
		 */
		private Tile[][] tiles;
		/**
		 * The width of each tile which is drawn to the screen
		 */
		private int tileWidth = 25;
		/**
		 * The height of each tile which is drawn to the screen
		 */
		private int tileHeight = 25;

		/**
		 * Stores the start X,Y location in terms of array indices within the 2d
		 * tile map
		 */
		private int startX, startY;
		/**
		 * Stores the end/goal X,Y location in terms of array indices within the
		 * 2d tile map
		 */
		private int endX, endY;

		/**
		 * Path finding variables
		 */
		// The path finding heuristic wanted. This can be changed by typing 1,
		// 2, 3 (At the time of writing)
		private PathFinding.Heuristic HEURISTIC_WANTED = PathFinding.Heuristic.EUCLIDIAN_DISTANCE;
		private PathFinding.Expansion EXPANSION_TYPE = PathFinding.Expansion.FOUR_SQUARE;
		private PathFinding.TieBreaking TIE_BREAKING_TYPE = PathFinding.TieBreaking.NONE;

		// ----------------------------------------------------------------
		// Color fields
		// ----------------------------------------------------------------
		private Map<String, Color> key = new HashMap<String, Color>() {
			{
				put("startLocationColor", Color.green);
				put("endLocationColor", Color.red);
				put("bestPathColor", Color.DARK_GRAY);
				put("closedTilesColor", Color.gray);
				put("openTilesColor", Color.orange);
			}
		};

		/**
		 * P is the player position (starting)
		 * <p>
		 * X is the ending (goal)
		 * <p>
		 * B is blocked
		 * <p>
		 * NOTE :: Try to choose tile weights which don't actually match the
		 * char's integer value.. I'm aware that you wouldn't use a 2d array of
		 * ints, it's just for demonstration
		 */
		private static final int P = '0', X = 'X', B = 'B';

		/**
		 * <p>
		 * Nasty way of creating tiles <blockquote>
		 * 
		 * <pre>
		 * 0-n is the 'weight' of each tile
		 *  B means it is impassable
		 * P is the player
		 * X is the goal
		 * 
		 * <pre>
		 * </blockquote>
		 * NOTE :: Try to choose tile weights which don't actually match the char's
		 * integer value, IE B is a char, but java will happily accept it within a
		 * 2d int. So don't choose an integer value that actually corresponds to the
		 * char pointers..Again, I know that you  wouldn't use a 2d array of ints,
		 * it's just for demonstration
		 */
		// @formatter:off
		private int[][] tileWeights = new int[][] {
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, B, B, B, B, B, B, B, B, B, B, B, B, B, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, P, 0, 0, X, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B, 0, 0, 0, 0},
				{ 0, 0, B, B, B, B, B, B, B, B, B, B, B, B, B, B, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		};
		// @formatter:on

		// Stores the best path found
		private List<Location> bestPath;

		// Stores all of the 'closed tiles' within the A* path finding
		// algorithm. This is kept track of so we can hover our mouse over
		// specific tiles and get the debug information required
		// Note, this will obviously contain the 'best path' found also.
		private Map<Location, SearchNode> closedTiles;
		private Map<Location, SearchNode> openTiles;

		private Map<Location, SearchNode> allTiles;

		protected TiledWorld() {
			super("tiled world");

			// Populate our tiles
			tiles = new Tile[tileWeights[0].length][tileWeights.length];

			for (int y = 0; y < tileWeights.length; y++) {
				for (int x = 0; x < tileWeights[0].length; x++) {
					/**
					 * NOTE :: Try to choose tile weights which don't actually
					 * match the char's integer value.. I'm aware that you
					 * wouldn't use a 2d array of ints, it's just for
					 * demonstration
					 */
					switch (tileWeights[y][x]) {
						case P:
							startX = x;
							startY = y;
							tiles[x][y] = new Tile(true, 0, Color.white, tileWidth, tileHeight);
							break;
						case X:
							endX = x;
							endY = y;
							tiles[x][y] = new Tile(true, 0, Color.white, tileWidth, tileHeight);
							break;
						case B:
							tiles[x][y] = new Tile(false, 0, Color.black, tileWidth, tileHeight);
							break;
						default:
							tiles[x][y] = new Tile(true, tileWeights[y][x], Color.white, tileWidth, tileHeight);
					}
				}
			}
		}

		@Override
		public void startUp(GameTime gameTime) {
			super.logicUpdate(gameTime);
			getPath();
			keyEventHandler.registerEvents(this);
		}

		@Override
		public void cleanUp() {
			super.cleanUp();
			keyEventHandler.removeEvents(this);
		}

		@Override
		public void logicUpdate(GameTime gameTime) {
			// checking if the mouse has moved within the bounds of the shown
			// map, so we can update the debug information
			if (mouseEventHandler.mousePosX < (tileWidth * tileWeights[0].length)
					&& mouseEventHandler.mouseClickPosY < (tileHeight * tileWeights.length)) {
				// Get the mouse location in terms of 'tile positions'
				int tileX = mouseEventHandler.mousePosX / tileWidth;
				int tileY = mouseEventHandler.mousePosY / tileHeight;
				Location tileLocation = new Location(tileX, tileY);
				SearchNode tile = allTiles.get(tileLocation);

				// If this is a valid tile, show the searchnode's details
				debugString = tile != null ?
						Helpers.concat("G :: ", tile.getCostG(),
								"\nH :: ", tile.getCostH(),
								"\nTotal :: ", tile.getTotalCost(),
								"\nVisit Order :: ", tile.getVisitOrder())
						: "Hover mouse over tile";
				debugString = Helpers.concat("Current Tile Info\n----------------\n", debugString);
			}

			// If we've clicked the mouse within the bounds of the map,
			// calculate where we clicked, then wecalculate everything required
			if (mouseEventHandler.leftClickDown || mouseEventHandler.rightClickDown) {
				if (mouseEventHandler.mouseClickPosX < (tileWidth * tileWeights[0].length)
						&& mouseEventHandler.mouseClickPosY < (tileHeight * tileWeights.length)) {
					// Get the mouse location in terms of 'tile positions'
					int tileX = mouseEventHandler.mouseClickPosX / tileWidth;
					int tileY = mouseEventHandler.mouseClickPosY / tileHeight;

					// Set the new start position
					if (mouseEventHandler.leftClickDown) {
						startX = tileX;
						startY = tileY;
					} else if (mouseEventHandler.rightClickDown) {
						endX = tileX;
						endY = tileY;
					}
					getPath();
				}
			}
		}

		/**
		 * When the user presses a key we check to see if a number between 1-9
		 * has been pressed, and if so we will change the heuristic wanted
		 * within the path finding test
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			// Get the char typed
			char typedChar = e.getKeyChar();
			// If the typed char was between 1-9 (in terms of the char datatype)
			if (typedChar > '0' && typedChar <= '9') {
				// Calculate the integer value of the char, subtracting the char
				// '1'. This will be the indice value for getting the herustic
				// type from the enums provided. IE, if the user types '1' on
				// the keyboard, indice value will be 0
				int indiceValue = (int) e.getKeyChar() - '1';
				// If the desired indice value is offered by the enum types,
				// change the herustic wanted and get the new best path
				if (indiceValue < PathFinding.Heuristic.values().length) {
					HEURISTIC_WANTED = PathFinding.Heuristic.values()[indiceValue];
					logger.info("Heuristic changed to :: ", HEURISTIC_WANTED);
					getPath();
				}
			}
		}

		public void getPath() {
			// Find our path
			AStarContainer container = PathFinding.findPathDebug(startX, startY, endX, endY, tiles,
					HEURISTIC_WANTED, TIE_BREAKING_TYPE, EXPANSION_TYPE);

			closedTiles = new HashMap<Location, SearchNode>();
			openTiles = new HashMap<Location, SearchNode>();
			allTiles = new HashMap<Location, SearchNode>();

			for (SearchNode tile : container.closedTiles) {
				closedTiles.put(tile.getAsLocation(), tile);
			}

			for (SearchNode tile : container.openTiles) {
				openTiles.put(tile.getAsLocation(), tile);
			}

			allTiles.putAll(closedTiles);
			allTiles.putAll(openTiles);

			// Populate a map so we can easily show statistics
			bestPath = new ArrayList<Location>();

			// Get the best path and add it to our best path map
			SearchNode parent = container.endTile;
			while ((parent = parent.getParent()) != null) {
				bestPath.add(parent.getAsLocation());
			}

			// Since we traverse from the end to the start when working out the
			// best path, reverse it, so it's more 'logical' (start to finish)
			Collections.reverse(bestPath);

			// Output info
			foundPathInString = Helpers.concat("\nA* Facts\n--------------",
					"\nTotal open :: ", container.openTiles.size(),
					"\nTotal Closed :: ", container.closedTiles.size(),
					"\nTotal best path :: ", bestPath.size(),
					"\nTotal :: ", container.openTiles.size() + container.closedTiles.size(),
					"\nMethod Used :: ", HEURISTIC_WANTED,
					"\n(PRESS 1-3 TO CHANGE)");
		}

		// Draw the tiles so we can see what path was found etc.
		@Override
		public void draw(Graphics2D drawScreen) {
			// Draw the grid
			for (int x = 0; x < tileWeights[0].length; x++) {
				for (int y = 0; y < tileWeights.length; y++) {
					// Draw the actual filled rect
					drawScreen.setColor(tiles[x][y].color);
					drawScreen.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
				}
			}

			// Draw all closed paths and (contains best path also, but as we
			// draw the best path over, it doesn't matter visually)
			drawScreen.setColor(key.get("closedTilesColor"));
			for (Location location : closedTiles.keySet()) {
				drawScreen.fillRect(location.getX() * tileWidth,
						location.getY() * tileHeight,
						tileWidth, tileHeight);
			}

			// draw all of the tiles which were opened, but not visited
			drawScreen.setColor(key.get("openTilesColor"));
			for (Location location : openTiles.keySet()) {
				drawScreen.fillRect(location.getX() * tileWidth,
						location.getY() * tileHeight,
						tileWidth, tileHeight);
			}

			// Draw the best path
			Location previousTile = null;
			int i = 0;
			for (Location location : bestPath) {
				drawScreen.setColor(key.get("bestPathColor"));
				drawScreen.fillRect(location.getX() * tileWidth, location.getY() * tileHeight,
						tileWidth, tileHeight);

				if (previousTile != null && i < bestPath.size()) {
					drawScreen.setColor(Color.blue);
					drawScreen.drawLine((previousTile.getX() * tileWidth) + (tileWidth / 2), (previousTile.getY() * tileHeight) + (tileHeight / 2),
							(location.getX() * tileWidth) + (tileWidth / 2), (location.getY() * tileHeight) + (tileHeight / 2));
				}

				previousTile = location;
				i++;
			}

			// Draw the start and end goal
			drawScreen.setColor(key.get("startLocationColor"));
			drawScreen.fillRect(startX * tileWidth, startY * tileHeight,
					tileWidth, tileHeight);

			drawScreen.setColor(key.get("endLocationColor"));
			drawScreen.fillRect(endX * tileWidth, endY * tileHeight,
					tileWidth, tileHeight);

			// Draw the grid's lines
			for (int x = 0; x < tileWeights[0].length; x++) {
				for (int y = 0; y < tileWeights.length; y++) {
					drawScreen.setColor(Color.black);
					drawScreen.drawRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
				}
			}

			// Draw the key
			int x = (tileWidth * tileWeights[0].length) + 30;
			int y = 40;

			for (String keyStr : key.keySet()) {
				drawScreen.setColor(Color.black);
				drawScreen.drawString(keyStr, x, y);

				drawScreen.setColor(key.get(keyStr));
				drawScreen.fillRect(x + 140, y - drawScreen.getFontMetrics().getHeight(), 20, 20);
				drawScreen.setColor(Color.black);
				drawScreen.drawRect(x + 140, y - drawScreen.getFontMetrics().getHeight(), 20, 20);

				y += 40;
			}

			// Draw the debug tile that the mouse is over
			drawScreen.setColor(Color.black);
			for (String line : foundPathInString.split("\n")) {
				drawScreen.drawString(line, x, y += drawScreen.getFontMetrics().getHeight());
			}

			y += 40;

			// Draw the debug tile that the mouse is over
			drawScreen.setColor(Color.black);
			for (String line : debugString.split("\n")) {
				drawScreen.drawString(line, x, y += drawScreen.getFontMetrics().getHeight());
			}
		}
	}

	public static final class Tile implements ITile {
		public boolean passable;
		public int cost;
		public Color color;
		public int width;
		public int height;

		Tile(boolean passable, int cost, Color color, int width, int height) {
			this.passable = passable;
			this.cost = cost;
			this.color = color;
			this.width = width;
			this.height = height;
		}

		@Override
		public int getTerrainCost() {
			return 0;
		}

		@Override
		public boolean isPassable() {
			return passable;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}
}
