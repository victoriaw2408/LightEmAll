import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a game piece
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;

  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
  }

  // ... [ Your GamePiece class contents here]

  // Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the
  // power station
  //
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that
    // can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }

  // rotates the tile
  public void rotation() {
    boolean firstLeft = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = this.top;
    this.top = firstLeft;
  }

  // makes the line powered
  public void powerUp() {
    this.powered = true;
  }

  // makes the line no longer powered
  public void powerDown() {
    this.powered = false;
  }

  // checks if there is a left
  public boolean hasLeft() {
    return this.left;
  }

  // checks if there is a right
  public boolean hasRight() {
    return this.right;
  }

  // checks if there is a top
  public boolean hasTop() {
    return this.top;
  }

  // checks if there is a bottom
  public boolean hasBottom() {
    return this.bottom;
  }
}

//represents a Utils class
class Utils {
  Random rand;

  Utils(Random rand) {
    this.rand = rand;
  }

  Utils() {
    this.rand = new Random();
  }

  // randomizes the lines
  int randTiles(int min) {
    return this.rand.nextInt(min);
  }
}

// represents the world class
class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  Utils util;
  Random rand;
  boolean win;
  int sec;
  int mili;
  int min;
  int doubleNum;
  int countClicks;

  LightEmAll(int width, int height) {
    this.rand = new Random();
    this.height = height;
    this.width = width;
    this.mst = new ArrayList<Edge>();
    this.util = new Utils();
    this.board = this.makeBoard();
    this.edges();
    this.kruskal();
    this.kruskalBoard();
    this.randTile();
    this.powerRow = 0;
    this.powerCol = 0;
    this.searchHelp();
    this.win = false;
    this.sec = 0;
    this.mili = 0;
    this.min = 0;
    this.doubleNum = 0;
    this.countClicks = 0;
  }

  LightEmAll(int width, int height, Random rand) {
    this.rand = rand;
    this.height = height;
    this.width = width;
    this.mst = new ArrayList<Edge>();
    this.util = new Utils(rand);
    this.board = this.makeBoard();
    this.edges();
    this.kruskal();
    this.kruskalBoard();
    this.randTile();
    this.powerRow = 0;
    this.powerCol = 0;
    this.searchHelp();
    this.sec = 0;
    this.mili = 0;
    this.min = 0;
    this.doubleNum = 0;
    this.countClicks = 0;
  }

  // creates the tiles
  public WorldScene makeScene() {
    String clickNum = Integer.toString(this.countClicks);
    String secNum = Integer.toString(this.sec);
    String miliNum = Integer.toString(this.mili);
    String semi = ":";
    String minNum = Integer.toString(this.min);
    String doubleNum = Integer.toString(this.doubleNum);
    TextImage time = new TextImage("「" + doubleNum + minNum + semi + secNum + miliNum + "」", 25,
        Color.RED);
    TextImage clickCount = new TextImage("Click Count", 20, Color.RED);
    TextImage num = new TextImage("" + semi + clickNum, 20, Color.RED);
    BesideImage clicks = new BesideImage(clickCount, num);
    WorldScene world = new WorldScene(this.width, this.height);
    TextImage gameOver = new TextImage("You Win :D", height * 3, FontStyle.BOLD, Color.MAGENTA);
    TextImage finalCount = new TextImage("Your click count was " + this.countClicks + "",
        height * 3, FontStyle.BOLD, Color.MAGENTA);
    AboveImage finalText = new AboveImage(gameOver, finalCount);

    // if you don't win
    for (int h = 0; h < height; h++) {
      for (int w = 0; w < width; w++) {
        GamePiece tile = board.get(h).get(w);
        // if this tile has the power station on it
        if (tile.powerStation) {
          world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, true), w * 60 + 30, h * 60 + 30);
        }
        // if the tile is powered without the power station on it
        else if (tile.powered) {
          world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, false), w * 60 + 30, h * 60 + 30);
        }
        // the tile isn't powered and the power station isn't on it
        else {
          world.placeImageXY(tile.tileImage(60, 5, Color.BLACK, false), w * 60 + 30, h * 60 + 30);
        }
      }
      world.placeImageXY(time, width * 50, (height * 60) + 25);
      world.placeImageXY(clicks, width + 70, (height * 60) + 25);
    }
    // return win screen
    if (win) {
      world.placeImageXY(time, width * 50, (height * 60) + 25);
      world.placeImageXY(clicks, width + 70, (height * 60) + 25);
      world.placeImageXY(finalText, (width * 20) + 60, (height * 20) + 60);
    }
    return world;

  }

  // makes the board
  public ArrayList<ArrayList<GamePiece>> makeBoard() {
    ArrayList<ArrayList<GamePiece>> board = new ArrayList<ArrayList<GamePiece>>();
    // loops through every row
    for (int h = 0; h < this.height; h++) {
      ArrayList<GamePiece> thisRow = new ArrayList<GamePiece>();
      // loops through each column in the row
      for (int w = 0; w < this.width; w++) {
        GamePiece tile = new GamePiece(h, w);
        // adds each tile to the row arrayList
        thisRow.add(tile);
        // places the powerStation in the top left of the board
        if (h == 0 && w == 0) {
          tile.powerStation = true;
        }
      }
      // adds the row to the board in each row loop
      board.add(thisRow);
    }
    return board;
  }

  // recreats the board using kruskal method
  public void kruskalBoard() {

    // loops through every row
    for (Edge e : this.mst) {
      ArrayList<GamePiece> thisRow = new ArrayList<GamePiece>();
      GamePiece edge1 = e.fromNode;
      GamePiece edge2 = e.toNode;
      thisRow.add(edge1);
      thisRow.add(edge2);

      // edge 2 below edge 1
      if (edge1.row < edge2.row && edge1.col == edge2.col) {
        edge1.bottom = true;
        edge2.top = true;
      }
      // next to each other
      if (edge1.row == edge2.row && edge1.col < edge2.col) {
        edge1.right = true;
        edge2.left = true;
      }

      // edge 1 above edge 2
      else if (edge1.row > edge2.row && edge1.col == edge2.col) {
        edge1.top = true;
        edge2.bottom = true;
      }
      // next to each other
      else if (edge1.row == edge2.row && edge1.col > edge2.col) {
        edge1.left = true;
        edge2.right = true;
      }

      board.add(thisRow);
      this.randTile();
    }
  }

  //sorts the edges and weights
  void edges() {
    int val = height * width;
    for (int h = 0; h < this.height; h++) {
      for (int w = 0; w < this.width; w++) {
        GamePiece tile = board.get(h).get(w);
        if (h > 0) {
          Edge e = new Edge(tile, board.get(h - 1).get(w), this.rand.nextInt(val));
          if (mst.contains(e)) {
            return;
          }
          else {
            mst.add(e);
          }
        }
        else if (h < height - 1) {
          Edge e = new Edge(tile, board.get(h + 1).get(w), this.rand.nextInt(val));
          if (mst.contains(e)) {
            return;
          }
          else {
            mst.add(e);
          }
        }
        if (w > 0) {
          Edge e = new Edge(tile, board.get(h).get(w - 1), this.rand.nextInt(val));
          if (mst.contains(e)) {
            return;
          }
          else {
            mst.add(e);
          }
        }
        else if (w < width - 1) {
          Edge e = new Edge(tile, board.get(h).get(w + 1), this.rand.nextInt(val));
          if (mst.contains(e)) {
            return;
          }
          else {
            mst.add(e);
          }
        }
      }
    }
    mst.sort(new LowestEdge());
  }

  // key event implementation
  public void onKeyEvent(String key) {
    GamePiece pow = this.board.get(powerRow).get(powerCol);
    GamePiece uPow = pow;
    GamePiece dPow = pow;
    GamePiece lPow = pow;
    GamePiece rPow = pow;
    // the following if-statements ensure the powerRows and powerCols are never out
    // of bounds
    if ((powerRow - 1 >= 0)) {
      uPow = this.board.get(powerRow - 1).get(powerCol);
    }
    if ((powerRow + 1 <= height - 1)) {
      dPow = this.board.get(powerRow + 1).get(powerCol);
    }
    if ((powerCol - 1 >= 0)) {
      lPow = this.board.get(powerRow).get(powerCol - 1);
    }
    if ((powerCol + 1 <= width - 1)) {
      rPow = this.board.get(powerRow).get(powerCol + 1);
    }

    // changes position of the powerStation depending on key-press, does not move if
    // out of bounds
    if (key.equals("up") && uPow.bottom && pow.top) {
      if (this.powerRow - 1 < 0) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerRow = this.powerRow - 1;
        uPow.powerStation = true;
      }
    }

    if (key.equals("right") && rPow.left && pow.right) {
      if (this.powerCol + 1 > width - 1) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerCol = this.powerCol + 1;
        rPow.powerStation = true;
      }
    }

    if (key.equals("down") && dPow.top && pow.bottom) {
      if (this.powerRow + 1 > height - 1) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerRow = this.powerRow + 1;
        dPow.powerStation = true;
      }
    }
    if (key.equals("left") && lPow.right && pow.left) {
      if (this.powerCol - 1 < 0) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerCol = this.powerCol - 1;
        lPow.powerStation = true;
      }
    }
    else {
      return;
    }
    this.searchHelp();
    this.checkWin();
  }

  // checking winning conditions
  public void checkWin() {
    int count = 0;
    int winCount = height * width;
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        GamePiece tile = this.board.get(row).get(col);
        if (tile.powered) {
          count = count + 1;
        }
      }
    }
    if (count == winCount) {
      this.win = true;
      this.endOfWorld("YouWin");
    }
  }

  // randomizes the tiles
  public void randTile() {
    for (int h = 0; h < this.height; h++) {
      for (int w = 0; w < this.width; w++) {
        for (int i = 0; i < util.randTiles(3); i++) {
          board.get(h).get(w).rotation();
        }
      }
    }
  }

  // breath first search
  void searchHelp() {
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
    GamePiece powerStar = this.board.get(powerRow).get(powerCol);
    Queue<GamePiece> worklist = new LinkedList<GamePiece>();
    worklist.add(powerStar);
    // powers down if no longer connected
    for (int w = 0; w < width; w++) {
      for (int h = 0; h < height; h++) {
        GamePiece tile = this.board.get(h).get(w);
        tile.powerDown();
      }
    }
    // As long as the work list isn't empty...
    while (!worklist.isEmpty()) {
      GamePiece next = worklist.poll();
      next.powerUp();
      if (alreadySeen.contains(next)) {
        worklist.remove(next);
      }
      else if (!alreadySeen.contains(next)) {

        // top row
        if (next.row > 0) {
          GamePiece top = this.board.get(next.row - 1).get(next.col);
          if (top.hasBottom() && next.hasTop()) {
            worklist.add(top);
          }
        }
        // far left
        if (next.col > 0) {
          GamePiece left = this.board.get(next.row).get(next.col - 1);
          if (left.hasRight() && next.hasLeft()) {
            worklist.add(left);
          }
        }
        // bottom
        if (next.row < height - 1) {
          GamePiece bottom = this.board.get(next.row + 1).get(next.col);
          if (bottom.hasTop() && next.hasBottom()) {
            worklist.add(bottom);
          }
        }
        // far right
        if (next.col < width - 1) {
          GamePiece right = this.board.get(next.row).get(next.col + 1);
          if (right.hasLeft() && next.hasRight()) {
            worklist.add(right);
          }
        }
        alreadySeen.add(next);
      }
    }
  }

  // old breath first search
  //  void bfs() {
  //    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
  //    ArrayList<GamePiece> worklist = new ArrayList<GamePiece>();
  //    GamePiece powerStar = this.board.get(powerRow).get(powerCol);
  //    worklist.add(powerStar);
  //
  //    // powers down if no longer connected
  //    for (int w = 0; w < width; w++) {
  //      for (int h = 0; h < height; h++) {
  //        GamePiece tile = this.board.get(h).get(w);
  //        tile.powerDown();
  //      }
  //    }
  //    // As long as the work list isn't empty...
  //    while (!worklist.isEmpty()) {
  //      GamePiece next = worklist.remove(0);
  //      next.powerUp();
  //      if (!alreadySeen.contains(next)) {
  //
  //        // top row
  //        if (next.row > 0) {
  //          GamePiece top = this.board.get(next.row - 1).get(next.col);
  //          if (top.hasBottom() && next.hasTop()) {
  //            worklist.add(top);
  //          }
  //        }
  //        // far left
  //        if (next.col > 0) {
  //          GamePiece left = this.board.get(next.row).get(next.col - 1);
  //          if (left.hasRight() && next.hasLeft()) {
  //            worklist.add(left);
  //          }
  //        }
  //        // bottom
  //        if (next.row < height - 1) {
  //          GamePiece bottom = this.board.get(next.row + 1).get(next.col);
  //          if (bottom.hasTop() && next.hasBottom()) {
  //            worklist.add(bottom);
  //          }
  //        }
  //
  //        // far right
  //        if (next.col < width - 1) {
  //          GamePiece right = this.board.get(next.row).get(next.col + 1);
  //          if (right.hasLeft() && next.hasRight()) {
  //            worklist.add(right);
  //          }
  //
  //        }
  //        alreadySeen.add(next);
  //      }
  //    }
  //  }

  // if a node name maps to itself, then it is the representative;
  // otherwise, “follow the links” in the representatives map, and
  // recursively look up the representative for the current node’s parent.
  public GamePiece find(HashMap<GamePiece, GamePiece> map, GamePiece from) {
    GamePiece fromEdge = map.get(from);
    // if from is the same as the representative
    if (from == (fromEdge)) {
      return fromEdge;
    }
    // keep accumulating till they equal
    else {
      return find(map, fromEdge);
    }
  }

  // set the value of one representative’s representative to the other.
  void union(HashMap<GamePiece, GamePiece> map, GamePiece fromEdge, GamePiece toEdge) {
    map.put(find(map, fromEdge), find(map, toEdge));
  }

  // test kruskal
  // iterator over list of edges
  // bc each edge has the correct wiring
  // establish connection between game pieces changing boolean false to true
  // for every cell in the board

  // kruskal algorithms
  // test kruskal
  // iterator over list of edges
  // bc each edge has the correct wiring
  // establish connection between game pieces changing boolean false to true
  // for every cell in the board

   // kruskal algorithms
   public ArrayList<Edge> kruskal() {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    ArrayList<Edge> worklist = new ArrayList<Edge>(); // all edges in graph, sorted by edge weights;

    // for all the game pieces in the board
    // for loop each of them to the same piece
    worklist.addAll(this.mst);
    // find
    while (worklist.size() >= 1) {
     worklist.get(0);
     if (find(representatives, worklist.get(0).fromNode) == find(representatives,
         worklist.get(0).toNode)) {
       worklist.remove(0);
     }
     // union
     else {
       union(representatives, find(representatives, worklist.get(0).fromNode),
           find(representatives, worklist.get(0).toNode));
       worklist.remove(0);
     }
   }
   return mst;
 }



  // mouse clicking implementation
  public void onMouseClicked(Posn pos) {
    int cellWidth = 60;
    int cellHeight = 60;
    int c = Math.floorDiv(pos.x, cellWidth);
    int r = Math.floorDiv(pos.y, cellHeight);
    Posn cellCoor = new Posn(c * cellWidth, r * cellHeight);
    int cellRight = cellCoor.x + cellWidth;
    int cellBottom = cellCoor.y + cellHeight;

    if ((r > height - 1 || c > width - 1)) {
      return;
    }

    if (pos.x >= cellCoor.x && pos.x < cellRight && pos.y >= cellCoor.y && pos.y < cellBottom) {
      this.countClicks += 1;
      board.get(r).get(c).rotation();
      this.searchHelp();
      this.checkWin();

    }
  }

  // controls the clock in my world
  public void onTick() {
    if (!win) {
      this.mili += 1;
      if (this.mili % 10 == 0) {
        this.mili = 0;
        this.sec += 1;
      }
      if (this.sec % 6 == 0 && this.mili % 10 == 0) {
        this.sec = 0;
        this.min += 1;
      }
      if (this.sec % 6 == 0 && this.mili % 10 == 0 && this.min % 10 == 0) {
        this.min = 0;
        this.doubleNum += 1;
      }
    }
  }

}

// represents an edge
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;
  Random rand;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
    this.rand = new Random();
  }

  Edge(GamePiece fromNode, GamePiece toNode, int weight, Random rand) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
    this.rand = rand;
  }

}

// figures out the lowest edge in the mst
class LowestEdge implements Comparator<Edge> {

  public int compare(Edge weight1, Edge weight2) {
    return weight1.weight - weight2.weight;
  }

}

// examples class 
class ExamplesLight {
  GamePiece g1;
  GamePiece g2;
  GamePiece g3;
  GamePiece g4;
  GamePiece g5;

  GamePiece g6;
  GamePiece g7;
  GamePiece g8;
  GamePiece g9;
  GamePiece g10;
  GamePiece g11;
  GamePiece g12;
  GamePiece g13;
  GamePiece g14;
  GamePiece game1;
  GamePiece game2;
  GamePiece game3;
  GamePiece game4;
  GamePiece game5;

  GamePiece game6;
  GamePiece game7;
  GamePiece game8;
  GamePiece game9;
  GamePiece game10;
  GamePiece game11;
  GamePiece game12;
  GamePiece game13;
  GamePiece game14;

  WorldImage r1;

  Random random1;
  Utils util1;

  Random random2;
  Utils util2;

  Random random3;
  Utils util3;

  Random random4;
  Utils util4;

  LightEmAll board1;
  LightEmAll board2;
  LightEmAll board3;
  LightEmAll board4;
  LightEmAll board5;
  LightEmAll board6;
  LightEmAll board7;

  WorldScene scene1;
  WorldScene scene2;
  WorldScene scene3;
  WorldScene scene4;

  WorldImage vWire;
  WorldImage hWire;
  WorldImage PvWire;
  WorldImage PhWire;

  WorldScene cscene3;
  WorldImage r2;
  WorldImage r3;
  WorldImage r4;
  WorldImage c4;
  WorldImage win;
  WorldImage r5;
  WorldImage r6;
  WorldImage r7;
  WorldImage r8;
  WorldImage r9;
  WorldImage c9;

  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;
  Edge e9;
  Edge ed1;
  Edge ed2;
  Edge ed3;
  Edge ed4;

  ArrayList<Edge> a1;
  TextImage clock;

  String clickNum;
  TextImage clickText;
  TextImage finalCount;
  ArrayList<Edge> a2;

  HashMap<GamePiece, GamePiece> map;
  HashMap<GamePiece, GamePiece> nmap;

  LowestEdge le;

  void initData() {

    random1 = new Random(5);
    util1 = new Utils(this.random1);

    random2 = new Random(10);
    util2 = new Utils(this.random2);

    le = new LowestEdge();

    board1 = new LightEmAll(3, 3, new Random(4));
    board1.board.get(0).get(0).top = true;
    board1.board.get(0).get(1).top = true;
    board1.board.get(0).get(2).top = true;
    board1.board.get(1).get(0).right = true;
    board1.board.get(1).get(0).top = true;
    board1.board.get(1).get(0).bottom = true;

    g1 = new GamePiece(0, 0);
    g2 = new GamePiece(0, 1);
    g3 = new GamePiece(0, 2);
    g4 = new GamePiece(1, 1);
    g5 = new GamePiece(0, 0);

    g6 = new GamePiece(0, 0);
    g7 = new GamePiece(0, 1);
    g8 = new GamePiece(0, 2);
    g9 = new GamePiece(1, 0);
    g10 = new GamePiece(1, 1);

    game1 = new GamePiece(0, 0);  
    game1.right = true;
    game1.top = true;
    game1.left = true;
    game1.powerStation = true;
    game1.powered = true;  
    
    game2 = new GamePiece(0, 1);
    game2.left = true;
    game2.right = true;
    game2.powered = true;

    game3 = new GamePiece(0, 2);
    game4 = new GamePiece(1, 1);
    game5 = new GamePiece(0, 0);

    game6 = new GamePiece(0, 0);
    game7 = new GamePiece(0, 1);
    game8 = new GamePiece(0, 2);
   
    game9 = new GamePiece(1, 0);
    game9.right = true;
    game9.bottom = true;
    game9.top = true;

    game10 = new GamePiece(1, 1);
    game10.left = true;
    game10.right = true;
    game10.top = true;
    
    game11 = new GamePiece(1, 2);
    game12 = new GamePiece(2, 0);
    game12.left = true;
    game12.right = true;
    game12.top = true;  
    game13 = new GamePiece(2, 1);
    game13.left = true;
    game13.right = true;
    game13.top = true;
 
    game14 = new GamePiece(2, 2);
    
    e1 = new Edge(game2, game1, 0);
    e2 = new Edge(game10, game9, 0);
    e3 = new Edge(game1, game2, 1);
    e4 = new Edge(game1, game2, 1);
    e5 = new Edge(game1, game9, 2);
    
    e6 = new Edge(game2, game10, 3);
    
    e7 = new Edge(game9, game10, 3);
    e8 = new Edge(game10, game2, 3);

    
    a1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e4, e3, e5, e6, e7, e8 ));


    clock = new TextImage("「00:00」", 25, Color.red);
    clickText = new TextImage("Click Count", 20, Color.RED);
    finalCount = new TextImage("Your click count was 0", 6, FontStyle.BOLD, Color.MAGENTA);
    ed1 = new Edge(g6, g7, 4);
    ed2 = new Edge(g6, g9, 7);
    ed3 = new Edge(g7, g10, 5);
    ed4 = new Edge(g9, g10, 2);

    a2 = new ArrayList<Edge>(Arrays.asList(ed1, ed2, ed3, ed4));

    map = new HashMap<GamePiece, GamePiece>();
    map.put(g6, g6);
    map.put(g7, g6);
    map.put(g9, g9);
    map.put(g10, g10);
    map.put(g7, g7);

    nmap = new HashMap<GamePiece, GamePiece>();
    nmap.put(g6, g6);
    nmap.put(g7, g6);
    nmap.put(g9, g9);
    nmap.put(g10, g10);
    nmap.put(g7, g7);
    nmap.put(g6, g7);

    board2 = new LightEmAll(2, 3, new Random(6));
    board2.board.get(0).get(0).left = true;
    board2.board.get(1).get(0).left = true;
    board2.board.get(1).get(0).right = true;
    board2.board.get(1).get(0).bottom = true;
    board2.board.get(1).get(1).bottom = true;
    board2.board.get(1).get(1).left = true;
    board2.board.get(1).get(1).right = true;
    board2.board.get(1).get(1).top = true;
    board2.board.get(2).get(0).bottom = true;
    board2.board.get(2).get(1).right = true;

    scene1 = new WorldScene(3, 3);
    scene2 = new WorldScene(3, 2);

    board3 = new LightEmAll(3, 3);

    board4 = new LightEmAll(2, 2);
    board7 = new LightEmAll(2, 2, new Random(2));

    board5 = new LightEmAll(3, 3, new Random(5));
    board6 = new LightEmAll(3, 3, new Random(7));

    vWire = new RectangleImage(5, (60 + 1) / 2, OutlineMode.SOLID, Color.DARK_GRAY);
    hWire = new RectangleImage((60 + 1) / 2, 5, OutlineMode.SOLID, Color.DARK_GRAY);
    PvWire = new RectangleImage(5, (60 + 1) / 2, OutlineMode.SOLID, Color.YELLOW);
    PhWire = new RectangleImage((60 + 1) / 2, 5, OutlineMode.SOLID, Color.YELLOW);

    r1 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r1 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r1);
    r2 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r2 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, r2);
    r2 = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, PhWire, 0, 0, r2);
    r2 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r2);
    r3 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r3 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, r3);
    r4 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r4 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r4);
    r5 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r5 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, r5);
    r5 = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, PhWire, 0, 0, r5);
    r5 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r5);
    r5 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, PhWire, 0, 0, r5);
    r5 = new OverlayImage(
        new OverlayImage(new StarImage(60 / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(60 / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
        r5);
    r6 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r6 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, r6);
    r7 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r7 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r7);
    r8 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r8 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, r8);
    r8 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, PvWire, 0, 0, r8);
    r8 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, PhWire, 0, 0, r8);
    r9 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    r9 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, PhWire, 0, 0, r9);
    c9 = new OverlayImage(new RectangleImage(5, 5, OutlineMode.SOLID, Color.YELLOW),
        new RectangleImage(60, 60, OutlineMode.SOLID, Color.DARK_GRAY));
    c9 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, PvWire, 0, 0, c9);
    win = new TextImage("You Win :D", 9 * 4.2, FontStyle.BOLD, Color.MAGENTA);

    scene3 = new WorldScene(3, 3);
    scene3.placeImageXY(r1, 30, 30);
    scene3.placeImageXY(r2, 90, 30);
    scene3.placeImageXY(r3, 150, 30);
    scene3.placeImageXY(r4, 90, 30);
    scene3.placeImageXY(r5, 90, 90);
    scene3.placeImageXY(r6, 90, 150);
    scene3.placeImageXY(r7, 150, 30);
    scene3.placeImageXY(r8, 150, 90);
    scene3.placeImageXY(r9, 150, 150);
    scene3.placeImageXY(clock, 90, 175);
    scene3.placeImageXY(clickText, 220, 175);

    cscene3 = new WorldScene(3, 3);
    cscene3.placeImageXY(r1, 30, 30);
    cscene3.placeImageXY(r2, 90, 30);
    cscene3.placeImageXY(r3, 150, 30);
    cscene3.placeImageXY(r4, 90, 30);
    cscene3.placeImageXY(r5, 90, 90);
    cscene3.placeImageXY(r6, 90, 150);
    cscene3.placeImageXY(r7, 150, 30);
    cscene3.placeImageXY(r8, 150, 90);
    cscene3.placeImageXY(c9, 150, 150);
    cscene3.placeImageXY(new AboveImage(win, finalCount), 45, 45);
    cscene3.placeImageXY(clock, 90, 175);
    cscene3.placeImageXY(clickText, 220, 175);

    random3 = new Random(7);
    util3 = new Utils(this.random3);

    random4 = new Random(5);
    util4 = new Utils(this.random4);
  }

  // tests powerUp
  void testpowerUp(Tester t) {
    initData();
    t.checkExpect(this.g1.powered, false);
    this.g1.powerUp();
    t.checkExpect(this.g1.powered, true);

    t.checkExpect(this.g2.powered, false);
    this.g2.powerUp();
    t.checkExpect(this.g2.powered, true);

  }

  // tests for kruskalBoard
  void testKruskalBoard(Tester t) {
    initData();
    board4.mst = a2;
    g6.right = false;
    g6.bottom = false;
    g7.left = false;
    g7.bottom = false;
    g9.top = false;
    g9.right = false;
    g10.top = false;
    g10.left = false;

    // tests to ensure that existing edges connect gamepieces
    board4.kruskalBoard();
    t.checkExpect(g6.right, true);
    t.checkExpect(g6.bottom, true);
    t.checkExpect(g7.left, true);
    t.checkExpect(g7.bottom, true);
    t.checkExpect(g9.top, true);
    t.checkExpect(g9.right, true);
    t.checkExpect(g10.top, true);
    t.checkExpect(g10.left, true);
  }

  // tests powerDown
  void testpowerDown(Tester t) {
    initData();
    t.checkExpect(this.g1.powered, false);
    this.g1.powerDown();
    t.checkExpect(this.g1.powered, false);

    this.g2.powerUp();
    t.checkExpect(this.g2.powered, true);
    this.g2.powerDown();
    t.checkExpect(this.g2.powered, false);

  }

  // tests checkWin
  void testCheckWin(Tester t) {
    initData();
    // set at least one board3 tile to not powered
    board4.board.get(1).get(1).powered = false;

    // checks to ensure board3 is not won yet
    board4.checkWin();
    t.checkExpect(board3.win, false);
    board4.board.get(0).get(0).powered = true;
    board4.board.get(1).get(0).powered = true;
    board4.board.get(0).get(1).powered = true;
    board4.board.get(1).get(1).powered = true;

    // checks to ensure board3 is won
    board4.checkWin();
    t.checkExpect(board4.win, true);

    // sets at least one tile in board2 to not powered
    board2.board.get(1).get(1).powered = false;

    // checks to ensure board2 is not won yet
    board2.checkWin();
    t.checkExpect(board2.win, false);

    // sets all board2 tiles to powered on
    board2.board.get(0).get(0).powered = true;
    board2.board.get(0).get(1).powered = true;
    board2.board.get(1).get(0).powered = true;
    board2.board.get(1).get(1).powered = true;
    board2.board.get(2).get(0).powered = true;
    board2.board.get(2).get(1).powered = true;

    // checks to ensure board2 is won
    board2.checkWin();
    t.checkExpect(board2.win, true);
  }

  // tests hasLeft
  boolean testhasLeft(Tester t) {
    initData();
    this.g2.left = true;
    return t.checkExpect(this.g5.hasLeft(), false) && t.checkExpect(this.g2.hasLeft(), true);
  }

  // tests hasRight
  boolean testhasRight(Tester t) {
    initData();
    this.g2.right = true;
    return t.checkExpect(this.g1.hasRight(), false) && t.checkExpect(this.g2.hasRight(), true);
  }

  // tests hasTop
  boolean testhasTop(Tester t) {
    initData();
    this.g2.top = true;
    return t.checkExpect(this.g1.hasTop(), false) && t.checkExpect(this.g2.hasTop(), true);
  }

  // tests hasBottom
  boolean testhasBottom(Tester t) {
    initData();
    this.g2.bottom = true;
    return t.checkExpect(this.g1.hasBottom(), false) && t.checkExpect(this.g2.hasBottom(), true);
  }

  // tests rotation
  void testrotation(Tester t) {
    initData();
    this.g1.left = true;
    t.checkExpect(this.g1.left, true);
    this.g1.rotation();
    t.checkExpect(this.g1.left, false);

    this.g1.bottom = true;
    t.checkExpect(this.g1.bottom, true);
    this.g1.rotation();
    t.checkExpect(this.g1.bottom, false);

    this.g1.right = true;

    t.checkExpect(this.g1.right, true);
    this.g1.rotation();
    t.checkExpect(this.g1.right, false);

    this.g1.top = true;

    t.checkExpect(this.g1.top, true);
    this.g1.rotation();
    t.checkExpect(this.g1.top, false);

  }

  // tests makeBoard
  void testmakeBoard(Tester t) {
    initData();
    board1.makeBoard();
    t.checkExpect(this.board1.board, board1.board);
    board2.makeBoard();
    t.checkExpect(this.board2.board, board2.board);
  }

  // tests for onMouseClickes, AND tests to ensure countClicks is counting
  void testOnMouseClicked(Tester t) {
    initData();
    // checks out of bounds, ensures no crash
    this.board2.onMouseClicked(new Posn(300, 300));
    t.checkExpect(board2, board2);

    // sets initial tile values
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = true;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // checks starting countClicks value
    t.checkExpect(board2.countClicks, 0);

    // tests to check if tile faces a new direction after being clicked
    this.board2.onMouseClicked(new Posn(88, 138));
    t.checkExpect(board2.board.get(2).get(1).left, false);
    t.checkExpect(board2.board.get(2).get(1).top, false);
    t.checkExpect(board2.board.get(2).get(1).right, true);
    t.checkExpect(board2.board.get(2).get(1).bottom, false);

    this.board2.onMouseClicked(new Posn(88, 138));
    t.checkExpect(board2.board.get(2).get(1).left, false);
    t.checkExpect(board2.board.get(2).get(1).top, false);
    t.checkExpect(board2.board.get(2).get(1).right, false);
    t.checkExpect(board2.board.get(2).get(1).bottom, true);

    this.board2.onMouseClicked(new Posn(88, 138));
    t.checkExpect(board2.board.get(2).get(1).left, true);
    t.checkExpect(board2.board.get(2).get(1).top, false);
    t.checkExpect(board2.board.get(2).get(1).right, false);
    t.checkExpect(board2.board.get(2).get(1).bottom, false);

    this.board2.onMouseClicked(new Posn(88, 138));
    t.checkExpect(board2.board.get(2).get(1).left, false);
    t.checkExpect(board2.board.get(2).get(1).top, true);
    t.checkExpect(board2.board.get(2).get(1).right, false);
    t.checkExpect(board2.board.get(2).get(1).bottom, false);

    // checks to ensure countClicks is counting correctly
    t.checkExpect(board2.countClicks, 4);

    // new tile values
    board5.board.get(1).get(0).left = false;
    board5.board.get(1).get(0).top = true;
    board5.board.get(1).get(0).right = true;
    board5.board.get(1).get(0).bottom = false;

    // checks starting countClicks value
    t.checkExpect(board5.countClicks, 0);

    // checks to see if tile faces new directions after being clicked
    this.board5.onMouseClicked(new Posn(26, 97));
    t.checkExpect(board5.board.get(1).get(0).left, false);
    t.checkExpect(board5.board.get(1).get(0).top, false);
    t.checkExpect(board5.board.get(1).get(0).right, true);
    t.checkExpect(board5.board.get(1).get(0).bottom, true);

    this.board5.onMouseClicked(new Posn(26, 97));
    t.checkExpect(board5.board.get(1).get(0).left, true);
    t.checkExpect(board5.board.get(1).get(0).top, false);
    t.checkExpect(board5.board.get(1).get(0).right, false);
    t.checkExpect(board5.board.get(1).get(0).bottom, true);

    this.board5.onMouseClicked(new Posn(26, 97));
    t.checkExpect(board5.board.get(1).get(0).left, true);
    t.checkExpect(board5.board.get(1).get(0).top, true);
    t.checkExpect(board5.board.get(1).get(0).right, false);
    t.checkExpect(board5.board.get(1).get(0).bottom, false);

    this.board5.onMouseClicked(new Posn(26, 97));
    t.checkExpect(board5.board.get(1).get(0).left, false);
    t.checkExpect(board5.board.get(1).get(0).top, true);
    t.checkExpect(board5.board.get(1).get(0).right, true);
    t.checkExpect(board5.board.get(1).get(0).bottom, false);

    // checks to ensure countClicks is counting correctly
    t.checkExpect(board5.countClicks, 4);
  }

  // tests randTile
  boolean testrandTiles(Tester t) {
    initData();
    return t.checkExpect(this.util1.randTiles(1), 0) && t.checkExpect(this.util2.randTiles(3), 0)
        && t.checkExpect(this.util3.randTiles(7), 3) && t.checkExpect(this.util4.randTiles(8), 5);
  }

  // tests find 
  boolean testFind(Tester t) {
    initData();
    return t.checkExpect(board4.find(map, g6), g6) && t.checkExpect(board4.find(map, g10), g10)
        && t.checkExpect(board4.find(map, g9), g9)
        // even if assigned to different values initially, loops to find match
        && t.checkExpect(board4.find(map, g7), g7);
  }

  // tests union
  void testUnion(Tester t) {
    initData();
    board4.union(map, g6, g7);
    t.checkExpect(map, nmap);
  }

  // tests compare for LowestEdge
  boolean testCompare(Tester t) {
    initData();
    return t.checkExpect(le.compare(ed2, ed1), 3) && t.checkExpect(le.compare(ed2, ed3), 2)
        && t.checkExpect(le.compare(ed2, ed4), 5);
  }

  // tests randTile
  void testrandTile(Tester t) {
    initData();
    // 4 cross gamePiece
    t.checkExpect(board6.board.get(0).get(0).top, true);
    t.checkExpect(board6.board.get(0).get(0).bottom, true);
    t.checkExpect(board6.board.get(0).get(0).left, true);
    t.checkExpect(board6.board.get(0).get(0).right, true);

    this.board6.randTile();

    t.checkExpect(board6.board.get(0).get(0).top, true);
    t.checkExpect(board6.board.get(0).get(0).bottom, true);
    t.checkExpect(board6.board.get(0).get(0).left, true);
    t.checkExpect(board6.board.get(0).get(0).right, true);

    // 2 piece gamePiece
    t.checkExpect(board6.board.get(2).get(2).top, true);
    t.checkExpect(board6.board.get(2).get(2).bottom, false);
    t.checkExpect(board6.board.get(2).get(2).left, false);
    t.checkExpect(board6.board.get(2).get(2).right, true);

    this.board6.randTile();

    t.checkExpect(board6.board.get(2).get(2).top, false);
    t.checkExpect(board6.board.get(2).get(2).bottom, true);
    t.checkExpect(board6.board.get(2).get(2).left, true);
    t.checkExpect(board6.board.get(2).get(2).right, false);

    // a 3 T piece
    t.checkExpect(board6.board.get(0).get(2).top, true);
    t.checkExpect(board6.board.get(0).get(2).bottom, true);
    t.checkExpect(board6.board.get(0).get(2).left, true);
    t.checkExpect(board6.board.get(0).get(2).right, false);

    this.board6.randTile();

    t.checkExpect(board6.board.get(0).get(2).top, true);
    t.checkExpect(board6.board.get(0).get(2).bottom, true);
    t.checkExpect(board6.board.get(0).get(2).left, false);
    t.checkExpect(board6.board.get(0).get(2).right, true);
  }

  // tests onKeyEvent
  void testOnKeyEvent(Tester t) {
    initData();
    // sets tile and surrounding tiles to all connect
    GamePiece tile = board4.board.get(0).get(0);
    tile.powerStation = true;
    tile.right = true;
    tile.bottom = true;
    GamePiece rtile = board4.board.get(0).get(1);
    rtile.left = true;
    rtile.bottom = true;
    GamePiece drtile = board4.board.get(1).get(1);
    drtile.top = true;
    drtile.left = true;
    GamePiece dtile = board4.board.get(1).get(0);
    dtile.top = true;
    dtile.right = true;

    // tests to ensure tiles move in given key press directions if connected
    t.checkExpect(tile.powerStation, true);
    board4.onKeyEvent("right");
    t.checkExpect(tile.powerStation, false);
    t.checkExpect(rtile.powerStation, true);
    board4.onKeyEvent("down");
    t.checkExpect(rtile.powerStation, false);
    t.checkExpect(drtile.powerStation, true);
    board4.onKeyEvent("left");
    t.checkExpect(drtile.powerStation, false);
    t.checkExpect(dtile.powerStation, true);
    board4.onKeyEvent("up");
    t.checkExpect(dtile.powerStation, false);
    t.checkExpect(tile.powerStation, true);

    // if connecting tile does not have a path connecting to tile, powerStation does
    // not move
    dtile.top = false;
    board4.onKeyEvent("down");
    t.checkExpect(tile.powerStation, true);
    t.checkExpect(dtile.powerStation, false);

    // ensures powerStation does not go out of bounds
    board4.onKeyEvent("down");
    t.checkExpect(tile.powerStation, true);
  }

  // tests makeScene
  void testMakeScene(Tester t) {
    initData();
    board5.makeScene();
    // board where all but one tile is connected
    board5.board.get(1).get(1).powerStation = true;
    board5.board.get(1).get(2).left = true;
    board5.board.get(1).get(0).right = true;
    board5.board.get(1).get(1).left = false;
    board5.board.get(1).get(1).right = true;
    board5.board.get(1).get(1).top = true;
    t.checkExpect(this.board5.makeScene(), scene3);
    board5.board.get(1).get(1).rotation();
    t.checkExpect(this.board5.makeScene(), cscene3);
  }

  // test on tick
  void testOnTick(Tester t) {
    initData();
    board6.onTick();
    board6.win = true;
    t.checkExpect(board6.sec, 0);

    board6.win = false;
    board6.sec = 3;
    t.checkExpect(board6.sec, 3);
    board6.win = true;
    t.checkExpect(board6.sec, 3);

    board4.onTick();
    board4.win = false;
    board4.min = 4;
    board4.sec = 20;
    t.checkExpect(board4.min, 4);
    t.checkExpect(board4.sec, 20);
    board4.win = true;
    t.checkExpect(board4.min, 4);
    t.checkExpect(board4.sec, 20);
  }

  // test edges
  void testEdges(Tester t) {
    board1.edges();
    t.checkExpect(board1.mst.size(), 36);
    board4.edges();
    t.checkExpect(board4.mst.size(), 16);
  }

  // tests kruskal
  boolean testkruskal(Tester t) {
    initData();
    return t.checkExpect(this.board7.kruskal(), a1);
  }

  // test searchHelp

  // test searchHelp
  void testsearchHelp(Tester t) {
    initData();

    // what is connected
    t.checkExpect(board1.board.get(0).get(0).powered, true);
    t.checkExpect(board1.board.get(0).get(1).powered, true);
    t.checkExpect(board1.board.get(1).get(2).powered, true);

    // what's not connected
    t.checkExpect(board1.board.get(0).get(2).powered, true);
    t.checkExpect(board1.board.get(2).get(0).powered, false);
    t.checkExpect(board1.board.get(2).get(1).powered, false);
    t.checkExpect(board1.board.get(2).get(2).powered, false);

    // rotate tiles to connect
    board1.onMouseClicked(new Posn(29, 91));
    
    board1.onMouseClicked(new Posn(29, 91));
    board1.onMouseClicked(new Posn(29, 91));

    board1.searchHelp();

    t.checkExpect(board1.board.get(0).get(0).powered, true);
    t.checkExpect(board1.board.get(0).get(1).powered, true);
    t.checkExpect(board1.board.get(0).get(2).powered, true);
    t.checkExpect(board1.board.get(2).get(0).powered, true);
    t.checkExpect(board1.board.get(2).get(1).powered, true);
    t.checkExpect(board1.board.get(2).get(2).powered, true);

    // disconnect a tile
    board1.onMouseClicked(new Posn(29, 91));

    board1.searchHelp();

    // now it is connected
    t.checkExpect(board1.board.get(0).get(0).powered, true);
    
    
    // testing again but disconnecting tile
    initData();

    // what is connected
    t.checkExpect(board1.board.get(0).get(0).powered, true);
    t.checkExpect(board1.board.get(0).get(1).powered, true);
    t.checkExpect(board1.board.get(1).get(2).powered, true);

    // what's not connected
    t.checkExpect(board1.board.get(0).get(2).powered, true);
    t.checkExpect(board1.board.get(2).get(0).powered, false);
    t.checkExpect(board1.board.get(2).get(1).powered, false);
    t.checkExpect(board1.board.get(2).get(2).powered, false);

    // rotate tiles to connect
    board1.onMouseClicked(new Posn(151, 101));
    board1.onMouseClicked(new Posn(151, 101));

    board1.searchHelp();
    t.checkExpect(board1.board.get(0).get(0).powered, true);
    t.checkExpect(board1.board.get(0).get(1).powered, true);
    t.checkExpect(board1.board.get(1).get(1).powered, true);
    
    t.checkExpect(board1.board.get(1).get(2).powered, false);

  }

  // tests bigBang
  void testBigBang(Tester t) {
    initData();
    // you can see the final text scene on a 3x3
    // everything is on top of each other but it still works
    // you can see the everything better on a 7x7
    LightEmAll world = new LightEmAll(7,7);
    int worldWidth = (world.width * 60);
    int worldHeight = (world.height * 60 + 50);
    double tickRate = 1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }
}