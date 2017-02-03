package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import javax.imageio.ImageIO;

public class Maze {
	/** The coordinate of the maze's entry point. */
	private Coord start = new Coord(0, 0);
	
	/** The coordinate of the maze's exit point. */
	private Coord end = new Coord(0, 0);
	
	/** The image of the maze to parse. */
	private BufferedImage originalImage;
	
	/** A two-dimensional boolean array to represent the maze. "false" indicates a wall, and "true" indicates a non-wall. */
	private boolean[][] maze;
	
	/** The width of the maze to parse. */
	private int mazeWidth;
	
	/** The height of the maze to parse. */
	private int mazeHeight;
	
	/** The heuristic function used to estimate cost of traversal from one coordinate to another. */
	private BiFunction<Coord, Coord, Double> heuristicCalculator = (from, to) -> Math.hypot(to.x - from.x, to.y - from.y);
	
	/** Constructs a Maze object using a BufferedImage of the maze. The image should be formatted so that
	 * each pixel corresponds to one maze 'tile'. There are several preconditions for the maze:
	 * <ul>
	 * <li>There can only be two pixel colors: black (0x000000) and white (0xffffff).</li>
	 * <li>The edges and corners of the image must be walls (i.e. black pixels) with the exception of
	 * two openings for entry and exit.</li>
	 * <li>There must be two openings for the maze; no more, no less.</li>
	 * <li>The openings must be 1 px wide.</li>
	 * </ul>
	 * Violation of any of the preconditions will raise an exception.
	 * @param	bi	The BufferedImage object representing an image of the maze.
	 * @throws	IllegalArgumentException	if one of the preconditions is violated.
	 */
	public Maze(BufferedImage bi) {
		originalImage = bi;
		mazeWidth = bi.getWidth();		// Due to 1 px padding
		mazeHeight = bi.getHeight();	// Due to 1 px padding
		maze = new boolean[mazeHeight][mazeWidth];
		
		for(int y = 0; y < mazeHeight; y++) {
			for(int x = 0; x < mazeWidth; x++) {
				maze[y][x] = checkColor(bi.getRGB(x, y));
			}
		}
				
		findOpenings();
	}
	
	public Coord start() {
		return start;
	}
	
	public Coord end() {
		return end;
	}
	
	public void setHeuristic(BiFunction<Coord, Coord, Double> heuristicCalculator) {
		this.heuristicCalculator = heuristicCalculator;
		// Pythagorean heuristic: 2*Math.hypot(from.x - to.x, from.y - to.y);
		// Taxicab heuristic: Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
	
	public List<Coord> solve() {
		Map<Coord, Coord> cameFrom = new HashMap<>();		// For each coord, the coord from which it can be most efficiently reached.
		Map<Coord, Double> gScore = new HashMap<>();			// For each coord, the cost of getting from the start coord to that coord.
		Map<Coord, Double> fScore = new HashMap<>();			// For each coord, the cost of getting from the start coord to end coord through that coord.
		
		// Initialize gScore and fScore maps
		for(int y = 0; y < mazeHeight; y++)
			for(int x = 0; x < mazeHeight; x++)
				if(maze[y][x]) {
					Coord coordKey = new Coord(x, y);
					gScore.put(coordKey, Double.MAX_VALUE);
					fScore.put(coordKey, Double.MAX_VALUE);
				}
		
		gScore.put(start, 0.0);
		fScore.put(start, heuristicCalculator.apply(start, end));
		
		// Nodes already visited.
		Set<Coord> closedCoords = new HashSet<Coord>();
		
		// Nodes discovered but not evaluated.
		TreeSet<Coord> openCoords = new TreeSet<Coord>((c1, c2) -> Double.compare(fScore.get(c1), fScore.get(c2)));
		
		openCoords.add(start);
		
		while(!openCoords.isEmpty()) {
			System.out.println(openCoords.size());
			Coord currentCoord = openCoords.pollFirst();
			if(currentCoord.equals(end))
				return reconstructPath(cameFrom, currentCoord);
			
			closedCoords.add(currentCoord);
			
			for(Direction d : Direction.values()) {
				Coord adjacentCoord = currentCoord.add(d);
				
				if(adjacentCoord.x < 0 || adjacentCoord.y < 0 || adjacentCoord.x >= mazeWidth || adjacentCoord.y >= mazeHeight)
					continue;
				
				if(!maze[adjacentCoord.y][adjacentCoord.x] || closedCoords.contains(adjacentCoord))
					continue;
				
				double tentativeGScore = gScore.get(currentCoord) + 1; // 1 step farther from start
				
				if(!openCoords.contains(adjacentCoord))
					openCoords.add(adjacentCoord);
				else if(tentativeGScore >= gScore.get(adjacentCoord))
					continue;
				
				cameFrom.put(adjacentCoord, currentCoord);
				gScore.put(adjacentCoord, tentativeGScore);
				fScore.put(adjacentCoord, gScore.get(adjacentCoord) + heuristicCalculator.apply(adjacentCoord, end));
			}
		}
		
		// In the event that a path is not found, return an empty ArrayList.
		return new ArrayList<Coord>();
	}
	
	private List<Coord> reconstructPath(Map<Coord, Coord> cameFrom, Coord end) {
		List<Coord> solution = new ArrayList<>();
		
		solution.add(end.copy());
		for(Coord c = end; cameFrom.containsKey(c); c = cameFrom.get(c))
			solution.add(c.copy());
		solution.add(start.copy());
		
		return solution;
	}
	
	private void findOpenings() throws IllegalArgumentException {
		for(int x = 0; x < mazeWidth; x++) {
			checkOpening(x, 0);
			checkOpening(x, mazeHeight-1);
		}
		for(int y = 0; y < mazeHeight; y++) {
			checkOpening(0, y);
			checkOpening(mazeWidth-1, y);
		}
		if(start.isZero() || end.isZero()) {
			throw new IllegalArgumentException("There are less than two openings in the maze image!");
		}
	}
	
	private boolean checkColor(int rgb) {
		boolean isWhite = (rgb == 0xffffffff); // -1 is the signed integer value for 0xffffffff
		boolean isBlack = (rgb == 0xff000000);
		if(!isWhite && !isBlack)
			throw new IllegalArgumentException("A non-black, non-white pixel exists in your image!");
		return isWhite;
	}
	
	private void checkOpening(int x, int y) throws IllegalArgumentException {
		if(maze[y][x]) {
			if(start.isZero())
				start = new Coord(x, y);
			else if(end.isZero())
				end = new Coord(x, y);
			else
				throw new IllegalArgumentException("There seems to be more than two openings in the maze image!");
		}
	}
	
	
	/* Maze should be in the same directory as the code */
	public static void main(String... args) throws Exception {
		Path p = Paths.get("maze.png");
		BufferedImage unsolvedMaze = ImageIO.read(p.toFile());
		Maze m = new Maze(unsolvedMaze);
		long start = System.currentTimeMillis();
		List<Coord> solution = m.solve();
		long end = System.currentTimeMillis();
		System.out.println(solution);
		System.out.println("Execution took " + (end - start) + " ms.");
		for(Coord c : solution) {
				if(m.originalImage.getRGB(c.x, c.y) == 0xff000000)
					System.err.println("Warning: Algorithm printed over wall!");
				m.originalImage.setRGB(c.x, c.y, 0xffff0000);
		}
		ImageIO.write(m.originalImage, "png", p.getParent().resolve("solvedmaze2.png").toFile());
	}
}