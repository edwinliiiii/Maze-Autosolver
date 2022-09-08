import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// a cell in our graph
class Cell {
  // top-left coordinates of our cell
  int x;
  int y;

  // if this cell is already visited
  boolean visited;

  // if this cell is contained in our final path
  boolean finale;

  // this cell's edges
  ArrayList<Edge> edges;

  Cell(int x, int y) {
    this.x = x;
    this.y = y;
    this.edges = new ArrayList<Edge>();
    this.visited = false;
    this.finale = false;
  }

  // the ID for this cell
  int cellID() {
    return (417 * this.x) + this.y;
  }
}

// an edge in a graph
class Edge {

  // initial connection
  Cell from;

  // final connection
  Cell to;

  int weight;

  Edge(Cell from, Cell to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}