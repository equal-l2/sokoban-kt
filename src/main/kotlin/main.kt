package main
import ansi.*

fun main(args:Array<String>){
  Sokoban.setStageInfo(1);
  while(true){
    Sokoban.printAllCells();
    if(Sokoban.isCleared()) break;
    Sokoban.inputMoveInfo();
    Sokoban.movePlayer();
  }
  println("\nCongratulations!");
}

object Sokoban {

  class Stage(var map: Array<Array<Int>>){
    class StageObj(var x:Int, var y:Int, val dispStr:String);
    val x:Int;
    val y:Int;
    var playerX = 0;
    var playerY = 0;
    init{
      x = map[0].size;
      y = map.size;
      for(a in map){
        if(a.size != x){
          throw IllegalArgumentException(String.format("y differs. map:%d y:%d",map.size,y));
        }
      }

      var cntDest   = 0;
      var cntPlayer = 0;
      var cntCrate  = 0;
      for (yidx in 0 until y){
        for (xidx in 0 until x){
          val obj = map[yidx][xidx];
          if(obj and DEST   != 0){
            cntDest++;
          }
          if(obj and PLAYER != 0){
            playerX = xidx;
            playerY = yidx;
            cntPlayer++;
          }
          if(obj and CRATE  != 0){
            cntCrate++;
          }
        }
      }
      if(cntDest < 1){
        throw IllegalArgumentException(String.format("Dest should be 1 at least. dest:%d",cntDest));
      }
      if(cntPlayer != 1){
        throw IllegalArgumentException(String.format("Player should be only 1. player:%d",cntPlayer));
      }
      if(cntCrate < 1){
        throw IllegalArgumentException(String.format("Crate should be 1 at least. crate:%d",cntCrate));
      }
      if(cntDest != cntCrate){
        throw IllegalArgumentException(String.format("Dests and crates don't match. dest:%d crate:%d",cntDest,cntCrate));
      }
    }
    companion object{
      val EMPTY  = 0b0000;
      val DEST   = 0b0001;
      val PLAYER = 0b0010;
      val CRATE  = 0b0100;
      val WALL   = 0b1000;
    }
  }

  lateinit var stage:Stage;

  var maxLength:Int = 0;

  var cellStr:Array<Array<String>> = arrayOf(
      //(back,fore,str)
      arrayOf("",              "",            "  "), // empty
      arrayOf(ansi.BG_GREEN,   "",            "DS"), // destination
      arrayOf(ansi.BG_RED,     "",            "PL"), // player
      arrayOf(ansi.BG_YELLOW,  "",            "PL"), // player on dest.
      arrayOf(ansi.BG_BLUE,    "",            "CR"), // crate.
      arrayOf(ansi.BG_CYAN,    "",            "CR"), // crate on dest.
      arrayOf("","",""),
      arrayOf("","",""),
      arrayOf(ansi.BG_MAGENTA, "",            "++")  // wall
  );

  var moveX = 0;
  var moveY = 0;
  var moveCrate = false;

  fun printAllCells() {
    ansi.clearScreen();
    for (y in 0 until stage.y){
      for (x in 0 until stage.x){
        printCell(x,y);
      }
    }
  }

  fun setStageInfo(stageNum:Int) {
    // stage objects are described as bit flags.
    // 0b0000 (0): empty
    // 0b0001 (1): destination
    // 0b0010 (2): player
    // 0b0011 (3): player on dest.
    // 0b0100 (4): crate
    // 0b0101 (5): crate on dest.
    // 0b0110 (6): (not in use)
    // 0b0111 (7): (not in use)
    // 0b1000 (8): wall
    if (stageNum==1) {
      stage = Stage(
          arrayOf(
              arrayOf(8, 8, 8, 8, 8, 8),
              arrayOf(8, 8, 8, 0, 0, 8),
              arrayOf(8, 8, 8, 0, 0, 8),
              arrayOf(8, 0, 4, 1, 0, 8),
              arrayOf(8, 0, 0, 2, 0, 8),
              arrayOf(8, 8, 8, 4, 1, 8),
              arrayOf(8, 8, 8, 0, 0, 8),
              arrayOf(8, 8, 8, 8, 8, 8)
          )
      );
    }
  }

  fun isCleared(): Boolean {
    for (yObj in stage.map){
      for (xObj in yObj){
        if(xObj == Stage.CRATE) return false;
      }
    }
    return true;
  }

  fun getCellStr(kind:Int) = cellStr[kind].reduce{a,b -> a+b}+ansi.RESET;

  fun printCell(x:Int, y:Int) {
    setLocation(x,y);
    print(getCellStr(stage.map[y][x]));
  }

  fun setLocation(x:Int, y:Int){
    val locateX = x * 2 + 3;
    val locateY = y + 2;
    ansi.locateCursor( locateX, locateY );
  }

  fun inputMoveInfo(){
    var buf:String = "";
    ansi.locateCursor(1,15);
    print("WASD: ");
    while(!(
        buf.equals("W") || buf.equals("w") ||
        buf.equals("S") || buf.equals("s") ||
        buf.equals("A") || buf.equals("a") ||
        buf.equals("D") || buf.equals("d") )
    ){
      buf = readLine()?:"";
    }

    if      (buf.equals("W") || buf.equals("w")) moveY = -1;
    else if (buf.equals("S") || buf.equals("s")) moveY =  1;
    else if (buf.equals("A") || buf.equals("a")) moveX = -1;
    else if (buf.equals("D") || buf.equals("d")) moveX =  1;
  }

  fun canMove(x: Int, y: Int, objIsPlayer: Boolean): Boolean {
    val toX = x + moveX;
    val toY = y + moveY;
    val toObj = stage.map[toY][toX];
    if (
        (toX < 0 || toX >= stage.x) || // out-of-bound
        (toY < 0 || toY >= stage.y) || // out-of-bound
        (toObj == Stage.WALL)          /* you are in rock */
    ) return false;

    if (toObj and Stage.CRATE != 0){
      if(objIsPlayer){
        moveCrate = true;
        return canMove(toX,toY,false);
      }
      else{
        return false;
      }
    }
    return true;
  }

  fun movePlayer(){
    val toX = stage.playerX + moveX;
    val toY = stage.playerY + moveY;
    if(canMove(stage.playerX,stage.playerY,true)){
      if(moveCrate){
        stage.map[toY][toX] = stage.map[toY][toX] and Stage.CRATE.inv();
        stage.map[toY+moveY][toX+moveX] = stage.map[toY+moveY][toX+moveX] or Stage.CRATE;
      }
      stage.map[stage.playerY][stage.playerX] = stage.map[stage.playerY][stage.playerX] and Stage.PLAYER.inv();
      stage.map[toY][toX] = stage.map[toY][toX] or Stage.PLAYER;
      stage.playerX = toX;
      stage.playerY = toY;
    }
    moveX = 0;
    moveY = 0;
    moveCrate = false;
  }
}
