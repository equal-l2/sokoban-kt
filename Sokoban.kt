fun main(args:Array<String>) = Sokoban.run();

object Sokoban {
  lateinit var stages:Array<Array<Int>>;
  var maxLength:Int = 0;
  var cellStr:Array<String> = arrayOf("  ", MyConsole.BG_BLUE+"++"+MyConsole.RESET, MyConsole.BG_RED+MyConsole.TEXT_WHITE+"PL"+MyConsole.RESET, "EN", "GO");
  var playerX:Int = 0;
  var playerY:Int = 0;
  var locateX:Int = 0;
  var locateY:Int = 0;
  var moveX:Int = 0;
  var moveY:Int = 0;
  var stageXSize:Int = 0;
  var stageYSize:Int = 0;

  fun run(){
    setStageInfo(1);
    while(true){
      printAllCells();
      inputMoveInfo();
      if (canMove()){
        movePlayer();
      }
    }
  }

  fun printAllCells() {
    MyConsole.clearScreen();
    for (y in 0 until stageYSize){
      for (x in 0 until stageXSize){
        if (x == playerX && y == playerY) {
          printPlayer(x,y);
        } else{
          printCell(x,y);
        }
      }
    }
  }

  fun setStageInfo(stage:Int) {
    if (stage==1) {
      val stagesBuf = arrayOf(
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
      );
      stages = stagesBuf;
      playerX = 1;
      playerY = 1;
      stageXSize = 10;
      stageYSize = 10;
    }
  }

  fun printCell(x:Int, y:Int) {
    setLocation(x,y);
    print(cellStr[stages[y][x]]);
  }

  fun printPlayer(x:Int, y:Int) {
    setLocation(x,y);
    print(cellStr[2]);
  }

  fun setLocation(x:Int, y:Int){
    locateX = x * 2 + 3;
    locateY = y + 2;
    MyConsole.locateCursor( locateX, locateY );
  }

  fun inputMoveInfo(){
    MyConsole.locateCursor(1,15);
    System.out.print("WASD: ");
    var buf:String = "";
    moveX = 0;
    moveY = 0;
    while(!buf.equals("W") && !buf.equals("w") &&
          !buf.equals("S") && !buf.equals("s") &&
          !buf.equals("A") && !buf.equals("a") &&
          !buf.equals("D") && !buf.equals("d") ){
      buf = readLine()?:"";
    }

    if      (buf.equals("W") || buf.equals("w")) moveY = -1;
    else if (buf.equals("S") || buf.equals("s")) moveY =  1;
    else if (buf.equals("A") || buf.equals("a")) moveX = -1;
    else if (buf.equals("D") || buf.equals("d")) moveX =  1;
  }

  fun canMove():Boolean{
    if (playerX + moveX < 0 || playerX + moveX >= stageXSize) return false;
    if (playerY + moveY < 0 || playerY + moveY >= stageYSize) return false;
    if (stages[playerY+moveY][playerX+moveX]==1) return false;
    return true;
  }

  fun movePlayer(){
    playerX += moveX;
    playerY += moveY;
  }
}
