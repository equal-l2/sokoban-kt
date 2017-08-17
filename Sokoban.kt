fun main(args:Array<String>){
  Sokoban.setStageInfo(1);
  while(true){
    Sokoban.printAllCells();
    Sokoban.inputMoveInfo();
    Sokoban.movePlayer();
  }
}

object Sokoban {

  class Stage(val map:Array<Array<Int>>){
    val xSize:Int;
    val ySize:Int;
    init{
      xSize = map.size;
      ySize = map[0].size;
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
      ANSI.BG_BLUE+"++"+ANSI.RESET,                   // wall
      ANSI.BG_RED+ANSI.FG_WHITE+"PL"+ANSI.RESET, // player
      "CR",                                                     // crate
      "DS"                                                      // destination
  );
  var playerX:Int = 0;
  var playerY:Int = 0;
  var moveX:Int = 0;
  var moveY:Int = 0;

  fun printAllCells() {
    ANSI.clearScreen();
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
        )
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
    ANSI.locateCursor( locateX, locateY );
  }

  fun inputMoveInfo(){
    var buf:String = "";
    moveX = 0;
    moveY = 0;
    ANSI.locateCursor(1,15);
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
