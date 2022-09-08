import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// our maze world
class MazeWorld extends World {

  // window size & scale
  public static int WORLD_WIDTH = 100;
  public static int WORLD_HEIGHT = 60;
  public static int WORLD_SCALE = 10;

  // all the cells in our maze
  ArrayList<ArrayList<Cell>> cells;

  // all the walls in our maze
  ArrayList<Edge> walls;

  boolean bfs;
  boolean dfs;
  BFS breadth;
  DFS depth;

  MazeWorld() {
    this.initialize();
  }

  // for initialize testing
  MazeWorld(int x) {
    WORLD_HEIGHT = 60;
    WORLD_WIDTH = 100;
  }

  // handles each key press of our maze game
  public void onKeyEvent(String key) {
    // sets up a whole new BFS using the same board
    if (key.equals("b")) {
      this.bfs = true;
      this.dfs = false;

      for (int i = 0; i < this.cells.size(); i++) {
        for (int j = 0; j < this.cells.get(i).size(); j++) {
          this.cells.get(i).get(j).visited = false;
          this.cells.get(i).get(j).finale = false;
        }
      }

      this.breadth = new BFS(this.cells);
    }

    // sets up a whole new DFS using the same board
    if (key.equals("d")) {
      this.bfs = false;
      this.dfs = true;

      for (int i = 0; i < this.cells.size(); i++) {
        for (int j = 0; j < this.cells.get(i).size(); j++) {
          this.cells.get(i).get(j).visited = false;
          this.cells.get(i).get(j).finale = false;
        }
      }

      this.depth = new DFS(this.cells);
    }

    // resets and reinitializes the maze completely [for extra credit]
    // this can take a while to load by the way...
    if (key.equals("r")) {
      this.initialize();
    }
  }

  // handles each tick of our maze game
  public void onTick() {
    // if BFS is active
    if (this.bfs) {
      if (this.breadth.hasNext()) {
        this.breadth.next();
      }
    }

    // if DFS is active
    if (this.dfs) {
      if (this.depth.hasNext()) {
        this.depth.next();
      }
    }
  }

  @Override
  // constructs our world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(WORLD_WIDTH * WORLD_SCALE, WORLD_HEIGHT * WORLD_SCALE);

    for (int i = 0; i < this.cells.size(); i++) {
      for (int j = 0; j < this.cells.get(i).size(); j++) {
        Cell temp = this.cells.get(i).get(j);
        Color color = getColor(temp);
        scene.placeImageXY(new RectangleImage(WORLD_SCALE, WORLD_SCALE, OutlineMode.SOLID, color),
            temp.x * WORLD_SCALE + (WORLD_SCALE / 2), temp.y * WORLD_SCALE + (WORLD_SCALE / 2));
      }
    }

    for (int i = 0; i < this.walls.size(); i++) {
      Edge temp = this.walls.get(i);
      if (temp.from.x != temp.to.x) {
        scene.placeImageXY(
            new RectangleImage(WORLD_SCALE / 10, WORLD_SCALE, OutlineMode.SOLID, Color.black),
            ((temp.from.x + temp.to.x) * WORLD_SCALE / 2 + WORLD_SCALE / 2),
            temp.to.y * WORLD_SCALE + WORLD_SCALE / 2);
      }
      else {
        scene.placeImageXY(
            new RectangleImage(WORLD_SCALE, WORLD_SCALE / 10, OutlineMode.SOLID, Color.black),
            temp.to.x * WORLD_SCALE + WORLD_SCALE / 2,
            (temp.from.y + temp.to.y) * WORLD_SCALE / 2 + WORLD_SCALE / 2);
      }
    }

    return scene;
  }

  // selects the correct color for the given cell
  public Color getColor(Cell c) {
    if (c.x == 0 && c.y == 0) {
      return Color.green;
    }
    if (c.x == WORLD_WIDTH - 1 && c.y == WORLD_HEIGHT - 1) {
      return Color.magenta;
    }
    if (c.finale) {
      return Color.PINK;
    }
    if (c.visited) {
      return Color.cyan;
    }
    return Color.LIGHT_GRAY;
  }

  // initializes our maze world
  public void initialize() {
    ArrayList<ArrayList<Cell>> allCells = generateCells();
    ArrayList<Edge> allEdges = generateEdges(allCells);
    allCells = updateKruskal(allCells);
    this.walls = generateWalls(allCells, allEdges);
    this.cells = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < allCells.size(); i++) {
      this.cells.add(new ArrayList<Cell>());
    }

    for (int i = 0; i < allCells.size(); i++) {
      for (int j = 0; j < allCells.get(i).size(); j++) {
        this.cells.get(i).add(allCells.get(i).get(j));
      }
    }

    this.bfs = false;
    this.dfs = false;
    this.breadth = new BFS(this.cells);
    this.depth = new DFS(this.cells);
  }

  // initializes the edges for our maze
  ArrayList<Edge> generateEdges(ArrayList<ArrayList<Cell>> cells) {
    ArrayList<Edge> finale = new ArrayList<Edge>();
    for (int i = 0; i < cells.size(); i++) {
      for (int j = 0; j < cells.get(i).size(); j++) {
        for (int k = 0; k < cells.get(i).get(j).edges.size(); k++) {
          finale.add(cells.get(i).get(j).edges.get(k));
        }
      }
    }

    return finale;
  }

  // generates the walls for our maze
  public ArrayList<Edge> generateWalls(ArrayList<ArrayList<Cell>> cells, ArrayList<Edge> edges) {
    ArrayList<Edge> temp = new ArrayList<Edge>();
    for (int i = 0; i < edges.size(); i++) {
      boolean validWall = true;
      for (int j = 0; j < cells.size(); j++) {
        for (int k = 0; k < cells.get(j).size(); k++) {
          for (int a = 0; a < cells.get(j).get(k).edges.size(); a++) {
            Edge e1 = edges.get(i);
            Edge e2 = cells.get(j).get(k).edges.get(a);
            if (e1.equals(e2) || (e1.from.equals(e2.to) && e1.to.equals(e2.from))) {
              validWall = false;
            }
          }
        }
      }

      if (validWall) {
        temp.add(edges.get(i));
      }
    }

    return temp;
  }

  // generates the cells and initializes its edges for our initial maze
  public ArrayList<ArrayList<Cell>> generateCells() {
    ArrayList<ArrayList<Cell>> temp = new ArrayList<ArrayList<Cell>>();
    for (int x = 0; x < WORLD_WIDTH; x++) {
      ArrayList<Cell> temp2 = new ArrayList<Cell>();
      for (int y = 0; y < WORLD_HEIGHT; y++) {
        temp2.add(new Cell(x, y));
      }
      temp.add(temp2);
    }

    Random rand = new Random();

    for (int i = 0; i < temp.size(); i++) {
      for (int j = 0; j < temp.get(i).size(); j++) {
        Cell c = temp.get(i).get(j);
        if (c.x > 0) {
          c.edges.add(new Edge(c, temp.get(i - 1).get(j), rand.nextInt(3000)));
        }
        if (c.x < WORLD_WIDTH - 1) {
          c.edges.add(new Edge(c, temp.get(i + 1).get(j), rand.nextInt(3000)));
        }
        if (c.y > 0) {
          c.edges.add(new Edge(c, temp.get(i).get(j - 1), rand.nextInt(3000)));
        }
        if (c.y < WORLD_HEIGHT - 1) {
          c.edges.add(new Edge(c, temp.get(i).get(j + 1), rand.nextInt(3000)));
        }
      }
    }

    return temp;
  }

  // updates the appropriate edges by weight for each cell
  public ArrayList<ArrayList<Cell>> updateKruskal(ArrayList<ArrayList<Cell>> cells) {
    // temporarily store all of our edges
    ArrayList<Edge> allEdges = generateEdges(cells);

    // initializes each cell's edges to be empty again
    for (int i = 0; i < cells.size(); i++) {
      for (int j = 0; j < cells.get(i).size(); j++) {
        cells.get(i).get(j).edges = new ArrayList<Edge>();
      }
    }

    // sorts all of our edges by weight
    Collections.sort(allEdges, new compareWeight());

    // initializes our identifying HashMap, (0, 0) -> (12313,12313)
    HashMap<Integer, Integer> unionFind = new HashMap<Integer, Integer>();

    for (int i = 0; i <= (417 * WORLD_WIDTH) + WORLD_HEIGHT; i++) {
      unionFind.put(i, i);
    }

    // adjust every edge to be appropriate
    for (int i = 0; i < allEdges.size(); i++) {
      Edge e = allEdges.get(i);
      // if not in the same group
      if (this.identify(unionFind, e.from.cellID()) != this.identify(unionFind, e.to.cellID())) {
        e.to.edges.add(new Edge(e.to, e.from, e.weight));
        e.from.edges.add(e);
        int holder = identify(unionFind, e.to.cellID());
        unionFind.remove(holder);
        unionFind.put(holder, identify(unionFind, e.from.cellID()));
      }
    }

    return cells;
  }

  // finds our identified value
  public int identify(HashMap<Integer, Integer> unionFind, int x) {
    if (unionFind.get(x) == x) {
      return x;
    }
    else {
      return identify(unionFind, unionFind.get(x));
    }
  }
}

// compares edges by weight
class compareWeight implements Comparator<Edge> {
  @Override
  // compares the weights of the given edges
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

// representing an arbitrary type of search
abstract class ASearch {
  // our cells that we've already visited in this search
  HashMap<String, Cell> cameFromEdge;

  // reconstructs our final path
  public void reconstruct(HashMap<String, Cell> hash, Cell c) {
    while (hash.containsKey(c.cellID() + "")) {
      c.finale = true;
      c = hash.get(c.cellID() + "");
    }
  }
}

// implements our breadth-first-search functionality
class BFS extends ASearch {
  // our worklist
  Deque<Cell> worklist;

  BFS(ArrayList<ArrayList<Cell>> cells) {
    this.worklist = new ArrayDeque<Cell>();
    this.worklist.addFirst(cells.get(0).get(0));
    cells.get(0).get(0).visited = true;
    cameFromEdge = new HashMap<String, Cell>();
  }

  // if there is a next element
  public boolean hasNext() {
    return !this.worklist.isEmpty();
  }

  // one iteration of the BFS
  public Deque<Cell> next() {
    Cell temp = this.worklist.remove(); // maybe
    // add all of the neighboring cells to the worklist as long as they are not
    // visited,
    // reconstructs the direct path if it reaches the end
    for (int i = 0; i < temp.edges.size(); i++) {
      Edge tempEdge = temp.edges.get(i);
      if (!tempEdge.to.visited) {
        cameFromEdge.put(tempEdge.to.cellID() + "", tempEdge.from);
        if (tempEdge.to.x == MazeWorld.WORLD_WIDTH - 1
            && tempEdge.to.y == MazeWorld.WORLD_HEIGHT - 1) {
          this.worklist.clear();
          this.reconstruct(this.cameFromEdge, tempEdge.to);
        }
        else {
          this.worklist.addLast(tempEdge.to);
          tempEdge.to.visited = true;
        }
      }
    }

    return this.worklist;
  }
}

// implements our depth-first-search functionality
class DFS extends ASearch {
  // our worklist
  Stack<Cell> worklist;

  DFS(ArrayList<ArrayList<Cell>> cells) {
    this.worklist = new Stack<Cell>();
    this.worklist.push(cells.get(0).get(0));
    cells.get(0).get(0).visited = true;
    cameFromEdge = new HashMap<String, Cell>();
  }

  // if there is a next element
  public boolean hasNext() {
    return !this.worklist.isEmpty();
  }

  // one iteration of the DFS
  public Stack<Cell> next() {
    Cell temp = this.worklist.pop();
    // add all of the neighboring cells to the worklist as long as they are not
    // visited,
    // reconstructs the direct path if it reaches the end
    for (int i = 0; i < temp.edges.size(); i++) {
      Edge tempEdge = temp.edges.get(i);
      if (!tempEdge.to.visited) {
        cameFromEdge.put(tempEdge.to.cellID() + "", tempEdge.from);
        if (tempEdge.to.x == MazeWorld.WORLD_WIDTH - 1
            && tempEdge.to.y == MazeWorld.WORLD_HEIGHT - 1) {
          this.worklist.clear();
          this.reconstruct(this.cameFromEdge, tempEdge.to);
        }
        else {
          this.worklist.push(tempEdge.to);
          tempEdge.to.visited = true;
        }
      }
    }

    return this.worklist;
  }
}

class ExamplesMaze {
  ArrayList<Edge> listEdge1;
  ArrayList<Edge> listEdge2;
  ArrayList<Edge> listEmpty;

  ArrayList<ArrayList<Cell>> cellEmpty;
  ArrayList<ArrayList<Cell>> listCell1;

  Cell c00 = new Cell(0, 0);
  Cell c01 = new Cell(0, 1);
  Cell c10 = new Cell(1, 0);
  Cell c11 = new Cell(1, 1);

  Edge e0001 = new Edge(c00, c01, 10);
  Edge e0010 = new Edge(c00, c10, 15);
  Edge e0111 = new Edge(c01, c11, 20);
  Edge e1011 = new Edge(c10, c11, 10);

  // initializes our testing conditions
  void initConditions() {
    c00.edges.clear();
    c01.edges.clear();
    c10.edges.clear();
    c11.edges.clear();

    // edge data
    listEdge1 = new ArrayList<Edge>(
        Arrays.asList(new Edge(null, null, 10), new Edge(null, null, 5), new Edge(null, null, 7)));

    // empty edge data
    listEmpty = new ArrayList<Edge>();

    // empty cell data
    cellEmpty = new ArrayList<ArrayList<Cell>>();

    // cell data
    listCell1 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(c00, c01)),
            new ArrayList<Cell>(Arrays.asList(c10, c11))));

    c00.edges.add(e0001);
    c00.edges.add(e0010);
    c01.edges.add(e0001);
    c01.edges.add(e0111);
    c10.edges.add(e0010);
    c10.edges.add(e1011);
    c11.edges.add(e0111);
    c11.edges.add(e1011);

    listEdge2 = new ArrayList<Edge>(
        Arrays.asList(e0001, e0010, e0001, e0111, e0010, e1011, e0111, e1011));
  }

  // tests compareWeight class method
  void testCompareWeight(Tester t) {
    initConditions();
    Collections.sort(listEdge1, new compareWeight());
    Collections.sort(listEmpty, new compareWeight());

    t.checkExpect(listEdge1.get(0), new Edge(null, null, 5));
    t.checkExpect(listEdge1.get(1), new Edge(null, null, 7));
    t.checkExpect(listEdge1.get(2), new Edge(null, null, 10));
    t.checkExpect(listEmpty.size(), 0);
  }

  // tests for the makeScene method
  void testMakeScene(Tester t) {
    initConditions();
    MazeWorld m = new MazeWorld();

    // making the scene
    WorldScene scene = m.makeScene();

    t.checkExpect(scene.height, 600);
    t.checkExpect(scene.width, 1000);
    t.checkExpect(m.walls.size(), 11682);
    t.checkExpect(m.cells.size(), 100);
  }

  // tests for the initializes method
  void testInialize(Tester t) {
    initConditions();
    MazeWorld m = new MazeWorld(0);

    m.initialize();

    t.checkExpect(m.cells.size(), 100);
    t.checkExpect(m.cells.get(0).size(), 60);

    t.checkExpect(m.walls.size(), 11682);
  }

  // tests for the generateWalls method
  void testGenerateWalls(Tester t) {
    initConditions();

    MazeWorld m = new MazeWorld(0);

    t.checkExpect(m.generateWalls(listCell1, listEdge2), new ArrayList<Edge>());
  }

  // tests for the generateEdges method
  void testGenerateEdges(Tester t) {
    initConditions();

    MazeWorld m = new MazeWorld(0);

    t.checkExpect(m.generateEdges(listCell1), listEdge2);

  }

  void testGetColor(Tester t) {
    MazeWorld m = new MazeWorld();

    Cell c = new Cell(5, 5);
    c.visited = true;
    c.finale = true;

    t.checkExpect(m.getColor(c), Color.pink);
    c.finale = false;
    t.checkExpect(m.getColor(c), Color.cyan);
    c.visited = false;
    t.checkExpect(m.getColor(c), Color.LIGHT_GRAY);
    c.x = MazeWorld.WORLD_WIDTH - 1;
    c.y = MazeWorld.WORLD_HEIGHT - 1;
    t.checkExpect(m.getColor(c), Color.magenta);
    c.x = 0;
    c.y = 0;
    t.checkExpect(m.getColor(c), Color.green);

  }

  // tests for the generateCells method
  void testGenerateCells(Tester t) {
    initConditions();
    MazeWorld m = new MazeWorld();

    m.generateCells();

    t.checkExpect(m.cells.size(), 100);
    t.checkExpect(m.cells.get(0).size(), 60);

    for (int i = 0; i < m.cells.size(); i++) {
      for (int j = 0; j < m.cells.get(0).size(); j++) {
        t.checkExpect(m.cells.get(0).get(0).edges.get(0).weight < 4000, true);
      }
    }
  }

  // tests for the identify method
  void testIdentify(Tester t) {
    HashMap<Integer, Integer> hMap = new HashMap<Integer, Integer>();
    MazeWorld m = new MazeWorld();

    hMap.put(2, 2);
    hMap.put(3, 3);
    hMap.put(4, 3);
    hMap.put(5, 4);

    t.checkExpect(m.identify(hMap, 2), 2);
    t.checkExpect(m.identify(hMap, 5), 3);
  }

  // tests for the updateKruskal method
  boolean testUpdateKruskal(Tester t) {
    initConditions();
    MazeWorld m = new MazeWorld();

    ArrayList<ArrayList<Cell>> allCells = m.generateCells();
    ArrayList<Edge> allEdges = m.generateEdges(allCells);

    return t.checkExpect(m.updateKruskal(allCells), allCells);
  }

  // run the game
  void testGame(Tester t) {
    MazeWorld m2 = new MazeWorld();
    m2.bigBang(MazeWorld.WORLD_WIDTH * MazeWorld.WORLD_SCALE,
        MazeWorld.WORLD_HEIGHT * MazeWorld.WORLD_SCALE, 0.005);
  }
}