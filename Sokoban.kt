fun main(args:Array<String>){
  Sokoban.setStageInfo(1);
  while(true){
    Sokoban.printAllCells();
    Sokoban.inputMoveInfo();
    Sokoban.movePlayer();
  }
}

object Sokoban {

  class Stage(val map:Array<Array<Int>>, val xSize:Int, val ySize:Int){
    init{
      if(map.size != xSize){
        throw IllegalArgumentException(String.format("xSize differs. map:%d xSize:%d",map.size,xSize))
      }
      for(a in map){
        if(a.size != ySize){
          throw IllegalArgumentException(String.format("ySize differs. map:%d ySize:%d",map.size,ySize))
        }
      }
    }
  }

  lateinit var stage:Stage;
  var maxLength:Int = 0;
  var cellStr:Array<String> = arrayOf(
      "  ",                                                     // empty
      MyConsole.BG_BLUE+"++"+MyConsole.RESET,                   // wall
      MyConsole.BG_RED+MyConsole.FG_WHITE+"PL"+MyConsole.RESET, // player
      "CR",                                                     // crate
      "DS"                                                      // destination
  );
  var playerX:Int = 0;
  var playerY:Int = 0;
  var moveX:Int = 0;
  var moveY:Int = 0;

  fun printAllCells() {
    MyConsole.clearScreen();
    for (y in 0 until stage.ySize){
      for (x in 0 until stage.xSize){
        if (x == playerX && y == playerY) {
          printPlayer(x,y);
        } else{
          printCell(x,y);
        }
      }
    }
  }

  fun setStageInfo(stageNum:Int) {
    if (stageNum==1) {
      stage = Stage(
        arrayOf(
          arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
          arrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
          arrayOf(1, 0, 1, 0, 1, 0, 1, 1, 0, 1),
          arrayOf(1, 0, 1, 0, 1, 0, 0, 0, 0, 1),
          arrayOf(1, 0, 0, 0, 0, 0, 1, 1, 0, 1),
          arrayOf(1, 0, 1, 1, 1, 0, 1, 1, 0, 1),
          arrayOf(1, 0, 1, 0, 1, 0, 0, 0, 0, 1),
          arrayOf(1, 0, 1, 0, 1, 0, 1, 1, 0, 1),
          arrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
          arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
        ),
        10,
        10
      );
      playerX = 1;
      playerY = 1;
    }
  }

  fun printCell(x:Int, y:Int) {
    setLocation(x,y);
    print(cellStr[stage.map[y][x]]);
  }

  fun printPlayer(x:Int, y:Int) {
    setLocation(x,y);
    print(cellStr[2]);
  }

  fun setLocation(x:Int, y:Int){
    val locateX = x * 2 + 3;
    val locateY = y + 2;
    MyConsole.locateCursor( locateX, locateY );
  }

  fun inputMoveInfo(){
    var buf:String = "";
    moveX = 0;
    moveY = 0;
    MyConsole.locateCursor(1,15);
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

  fun canMove():Boolean{
    if (playerX + moveX < 0 || playerX + moveX >= stage.xSize) return false;
    if (playerY + moveY < 0 || playerY + moveY >= stage.ySize) return false;
    if (stage.map[playerY+moveY][playerX+moveX]==1) return false;
    return true;
  }

  fun movePlayer(){
    if(canMove()){
      playerX += moveX;
      playerY += moveY;
    }
  }
}
