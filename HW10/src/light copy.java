import java.util.ArrayList;
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
  // ArrayList<Edge> mst;
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

  LightEmAll(int width, int height) {
    this.height = height;
    this.width = width;
    this.util = new Utils();
    this.board = this.makeBoard();
    this.randTile();
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.searchHelp();
    this.win = false;

  }

  LightEmAll(int width, int height, Random rand) {
    this.height = height;
    this.width = width;
    this.util = new Utils(rand);
    this.board = this.makeBoard();
    this.randTile();
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.searchHelp();

  }

  // creates the tiles
  public WorldScene makeScene() {
    WorldScene world = new WorldScene(this.width, this.height);
    // if you don't win
    if (!win) {
      for (int h = 0; h < height; h++) {
        for (int w = 0; w < width; w++) {
          GamePiece tile = board.get(h).get(w);
          // if this tile has the power station on it
          if (tile.powerStation) {
            world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, true), w * 60 + 30, h * 60 + 30);
          }
          // if the tile is powered without the power station on it
          else if (tile.powered) {
            world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, false), w * 60 + 30,
                h * 60 + 30);
          }
          // the tile isn't powered and the power station isn't on it
          else {
            world.placeImageXY(tile.tileImage(60, 5, Color.LIGHT_GRAY, false), w * 60 + 30,
                h * 60 + 30);
          }
        }
      }
      return world;
    }
    // if you do win return the win screen
    else {
      for (int h = 0; h < height; h++) {
        for (int w = 0; w < width; w++) {
          GamePiece tile = board.get(h).get(w);
          // if this tile has the power station on it
          if (tile.powerStation) {
            world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, true), w * 60 + 30, h * 60 + 30);
          }
          // if the tile is powered without the power station on it
          else if (tile.powered) {
            world.placeImageXY(tile.tileImage(60, 5, Color.YELLOW, false), w * 60 + 30,
                h * 60 + 30);
          }
          // the tile isn't powered and the power station isn't on it
          else {
            world.placeImageXY(tile.tileImage(60, 5, Color.LIGHT_GRAY, false), w * 60 + 30,
                h * 60 + 30);
          }
        }
      }
      // win screen
      TextImage gameOver = new TextImage("You Win :D", board.size() * 4.2, FontStyle.BOLD,
          Color.GREEN);
      world.placeImageXY(gameOver, width * 15, height * 15);
      return world;
    }
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
        // adds each tile to the row arraylist
        thisRow.add(tile);
        // places the powerStation in the middle of the board
        if (h == Math.floorDiv(this.height, 2) && w == Math.floorDiv(this.width, 2)) {
          tile.top = true;
          tile.right = true;
          tile.bottom = true;
          tile.left = true;
          tile.powerStation = true;
        }
        // if the tile is in the top row, the tile only has a wire going down
        else if (h == 0) {
          tile.bottom = true;
        }
        // if the tile is in the bottom row, the tile only has a wire going down
        else if (h == height - 1) {
          tile.top = true;
        }
        // if the tile is in the middle row, but not at either end of the row,
        // the tile has wires going each direction
        else if (h == Math.floorDiv(this.height, 2) && !(w == 0 || w == this.width - 1)) {
          tile.top = true;
          tile.right = true;
          tile.bottom = true;
          tile.left = true;
        }
        // if the tile is in the far left of the middle row, all directions but the left
        // have wires
        else if (h == Math.floorDiv(this.height, 2) && w == 0) {
          tile.top = true;
          tile.right = true;
          tile.bottom = true;
        }
        // if the tile is in the far right of the middle, all directions but the right
        // have wires
        else if (h == Math.floorDiv(this.height, 2) && w == this.width - 1) {
          tile.top = true;
          tile.bottom = true;
          tile.left = true;
        }
        // if the tile is anywhere else, the wires go up and down
        else {
          tile.top = true;
          tile.bottom = true;
        }
      }
      // adds the row to the board in each row loop
      board.add(thisRow);
    }
    return board;
  }

  // key event implementation
  public void onKeyEvent(String key) {
    GamePiece pow = this.board.get(powerRow).get(powerCol);
    GamePiece uPow = pow;
    GamePiece dPow = pow;
    GamePiece lPow = pow;
    GamePiece rPow = pow;
    // the following if-statements unsure the powerRows and powerCols are never out
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
    if (key.equals("up") && uPow.bottom) {
      if (this.powerRow - 1 < 0) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerRow = this.powerRow - 1;
        uPow.powerStation = true;
      }
    }

    if (key.equals("right") && rPow.left) {
      if (this.powerCol + 1 > width - 1) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerCol = this.powerCol + 1;
        rPow.powerStation = true;
      }
    }

    if (key.equals("down") && dPow.top) {
      if (this.powerRow + 1 > height - 1) {
        return;
      }
      else {
        pow.powerStation = false;
        this.powerRow = this.powerRow + 1;
        dPow.powerStation = true;
      }
    }
    if (key.equals("left") && lPow.right) {
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
        for (int i = 0; i < util.randTiles(4); i++) {
          board.get(h).get(w).rotation();
        }
      }
    }
  }

  // breath first search
  void searchHelp() {
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
    ArrayList<GamePiece> worklist = new ArrayList<GamePiece>();
    GamePiece powerStar = this.board.get(powerRow).get(powerCol);
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
      GamePiece next = worklist.remove(0);
      next.powerUp();
      if (!alreadySeen.contains(next)) {

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
      board.get(r).get(c).rotation();
      this.searchHelp();
      this.checkWin();

    }
  }
}

// represents a vertex
//class Vertex {
// ... any data about vertices, such as people's names, or place's GPS
//  // coordinates ...
//  ArrayList<Edge> outEdges; // edges from this node
//}
//
// represents an edge
//class Edge {
//  Vertex from;
//  Vertex to;
//  int weight;
//}
//
// represents a graph
//class Graph {
//  ArrayList<Vertex> allVertices;
//}

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

  WorldImage r1;

  Random random1;
  Utils util1;

  Random random2;
  Utils util2;

  LightEmAll board1;
  LightEmAll board2;
  LightEmAll board3;
  LightEmAll board4;
  LightEmAll board5;

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

  void initData() {
    g1 = new GamePiece(0, 0);
    g2 = new GamePiece(0, 1);
    g3 = new GamePiece(0, 2);
    g4 = new GamePiece(1, 1);
    g5 = new GamePiece(0, 0);

    random1 = new Random(5);
    util1 = new Utils(this.random1);

    random2 = new Random(10);
    util2 = new Utils(this.random2);

    board1 = new LightEmAll(3, 3, new Random(4));
    board1.board.get(0).get(0).top = true;
    board1.board.get(0).get(1).top = true;
    board1.board.get(0).get(2).top = true;
    board1.board.get(1).get(0).right = true;
    board1.board.get(1).get(0).top = true;
    board1.board.get(1).get(0).bottom = true;

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

    board5 = new LightEmAll(3, 3, new Random(5));

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
    win = new TextImage("You Win :D", 9 * 4.2, FontStyle.BOLD, Color.GREEN);

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
    cscene3.placeImageXY(win, 45, 45);

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

  void testOnMouseClicked(Tester t) {
    initData();
    // checks out of bounds, ensures no crash
    this.board2.onMouseClicked(new Posn(300, 300));
    t.checkExpect(board2, board2);

    // checks on a tile in the randomized board 2
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = true;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // tile faces the right but has no right neighbor connection
    this.board2.onMouseClicked(new Posn(88, 138));
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = false;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // tile faces the bottom but has no bottom tile neighbor connection
    this.board2.onMouseClicked(new Posn(88, 138));
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = false;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // tile faces the left but has no left neighbor connection
    this.board2.onMouseClicked(new Posn(88, 138));
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = false;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // returns back facing the top and has a top connected neighbor
    this.board2.onMouseClicked(new Posn(88, 138));
    board2.board.get(2).get(1).left = false;
    board2.board.get(2).get(1).top = true;
    board2.board.get(2).get(1).right = false;
    board2.board.get(2).get(1).bottom = false;

    // checks on a tile in the randomized board 5
    board2.board.get(1).get(0).left = false;
    board2.board.get(1).get(0).top = false;
    board2.board.get(1).get(0).right = true;
    board2.board.get(1).get(0).bottom = false;

    // tile is facing left but has no left neighbor to connect
    this.board5.onMouseClicked(new Posn(26, 97));
    board2.board.get(1).get(0).left = false;
    board2.board.get(1).get(0).top = false;
    board2.board.get(1).get(0).right = false;
    board2.board.get(1).get(0).bottom = false;

    // tile is facing top but can connect to right neighbor
    this.board5.onMouseClicked(new Posn(26, 97));
    board2.board.get(1).get(0).left = false;
    board2.board.get(1).get(0).top = false;
    board2.board.get(1).get(0).right = true;
    board2.board.get(1).get(0).bottom = false;

    // tile is facing right but can connect to right neighbor
    this.board5.onMouseClicked(new Posn(26, 97));
    board2.board.get(1).get(0).left = false;
    board2.board.get(1).get(0).top = false;
    board2.board.get(1).get(0).right = true;
    board2.board.get(1).get(0).bottom = false;

    // tile is facing down again but can connect to right neighbor
    this.board5.onMouseClicked(new Posn(26, 97));
    board2.board.get(1).get(0).left = false;
    board2.board.get(1).get(0).top = false;
    board2.board.get(1).get(0).right = true;
    board2.board.get(1).get(0).bottom = false;
  }

  // tests randTile
  boolean testrandTiles(Tester t) {
    initData();
    return t.checkExpect(this.util1.randTiles(1), 0) && t.checkExpect(this.util2.randTiles(3), 0);
  }

  // tests randTile
  void testrandTile(Tester t) {
    initData();

    t.checkExpect(board2.board.get(0).get(0).top, false);
    t.checkExpect(board2.board.get(0).get(0).bottom, false);
    t.checkExpect(board2.board.get(0).get(0).left, true);
    t.checkExpect(board2.board.get(0).get(0).right, false);

    this.board2.randTile();

    t.checkExpect(board2.board.get(0).get(0).top, false);
    t.checkExpect(board2.board.get(0).get(0).bottom, true);
    t.checkExpect(board2.board.get(0).get(0).left, false);
    t.checkExpect(board2.board.get(0).get(0).right, false);

  }

  // tests onKeyEvent
  void testOnKeyEvent(Tester t) {
    initData();
    // sets tile and surrounding tiles to all connect
    GamePiece tile = board4.board.get(1).get(1);
    tile.powerStation = true;
    tile.top = true;
    tile.left = true;
    GamePiece utile = board4.board.get(0).get(1);
    utile.left = true;
    utile.bottom = true;
    GamePiece lttile = board4.board.get(0).get(0);
    lttile.right = true;
    lttile.bottom = true;
    GamePiece ltile = board4.board.get(1).get(0);
    ltile.top = true;
    ltile.right = true;

    // tests to ensure tiles move in given key press directions if connected
    t.checkExpect(tile.powerStation, true);
    board4.onKeyEvent("up");
    t.checkExpect(tile.powerStation, false);
    t.checkExpect(utile.powerStation, true);
    board4.onKeyEvent("left");
    t.checkExpect(utile.powerStation, false);
    t.checkExpect(lttile.powerStation, true);
    board4.onKeyEvent("down");
    t.checkExpect(lttile.powerStation, false);
    t.checkExpect(ltile.powerStation, true);
    board4.onKeyEvent("right");
    t.checkExpect(ltile.powerStation, false);
    t.checkExpect(tile.powerStation, true);

    // if connecting tile does not have a path connecting to tile, powerStation does
    // not move
    utile.bottom = false;
    board4.onKeyEvent("up");
    t.checkExpect(tile.powerStation, true);
    t.checkExpect(utile.powerStation, false);

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

  // test searchHelp
  void testSearchHelp(Tester t) {
    initData();

    // what is connected
    t.checkExpect(board1.board.get(1).get(1).powered, true);
    t.checkExpect(board1.board.get(1).get(0).powered, true);
    t.checkExpect(board1.board.get(1).get(2).powered, true);

    // what's not connected
    t.checkExpect(board1.board.get(0).get(0).powered, false);
    t.checkExpect(board1.board.get(0).get(1).powered, false);
    t.checkExpect(board1.board.get(0).get(2).powered, false);
    t.checkExpect(board1.board.get(2).get(0).powered, false);
    t.checkExpect(board1.board.get(2).get(1).powered, false);
    t.checkExpect(board1.board.get(2).get(2).powered, false);

    // rotate the tiles to connect
    board1.onMouseClicked(new Posn(38, 33));

    board1.onMouseClicked(new Posn(82, 24));
    board1.onMouseClicked(new Posn(82, 24));

    board1.onMouseClicked(new Posn(120, 34));
    board1.onMouseClicked(new Posn(120, 34));
    board1.onMouseClicked(new Posn(120, 34));

    board1.onMouseClicked(new Posn(41, 142));
    board1.onMouseClicked(new Posn(41, 142));
    board1.onMouseClicked(new Posn(41, 142));
    board1.onMouseClicked(new Posn(157, 92));

    board1.onMouseClicked(new Posn(93, 145));
    board1.onMouseClicked(new Posn(93, 145));

    board1.onMouseClicked(new Posn(153, 156));
    board1.onMouseClicked(new Posn(153, 156));

    board1.searchHelp();

    t.checkExpect(board1.board.get(0).get(0).powered, true);
    t.checkExpect(board1.board.get(0).get(1).powered, true);
    t.checkExpect(board1.board.get(0).get(2).powered, true);
    t.checkExpect(board1.board.get(2).get(0).powered, true);
    t.checkExpect(board1.board.get(2).get(1).powered, true);
    t.checkExpect(board1.board.get(2).get(2).powered, true);

    // disconnect a tile
    board1.onMouseClicked(new Posn(34, 37));
    board1.onMouseClicked(new Posn(34, 37));

    board1.searchHelp();

    // now it is disconnected
    t.checkExpect(board1.board.get(0).get(0).powered, false);

  }

  // tests bigBang
  void testBigBang(Tester t) {
    initData();
    LightEmAll world = new LightEmAll(3, 3);
    int worldWidth = (world.width * 60 + 10);
    int worldHeight = (world.height * 60 + 10);
    double tickRate = .1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }
}