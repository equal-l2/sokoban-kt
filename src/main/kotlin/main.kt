package main
import ansi.*

fun main(args:Array<String>){
  Sokoban.setStageInfo(1);
  while(true){
    Sokoban.printAllCells();
    Sokoban.inputMoveInfo();
    Sokoban.movePlayer();
    if(Sokoban.isCleared()) break;
  }
  println("Congratulation!");
}

object Sokoban {

  class Stage(val map: Array<Array<Int>>, var stageObjs: Array<StageObj>){
    class StageObj(var x:Int, var y:Int, val dispStr:String);
    val x:Int;
    val y:Int;
    var player = stageObjs[0];
    init{
      x = map[0].size;
      y = map.size;
      for(a in map){
        if(a.size != x){
          throw IllegalArgumentException(String.format("y differs. map:%d y:%d",map.size,y))
        }
      }
      for(o in stageObjs){
        if ( o.x < 0 || o.x >= x) throw IllegalArgumentException(String.format("Object is out of bound. map:%d obj:%d",x,o.x))
        if ( o.y < 0 || o.y >= y) throw IllegalArgumentException(String.format("Object is out of bound. map:%d obj:%d",y,o.y))
      }
    }

    fun findCrate(x: Int, y: Int): Int {
      for(i in 1 until stageObjs.size){
        if(stageObjs[i].x == x && stageObjs[i].y == y) return i;
      }
      return -1;
    }
    companion object{
      fun getPlayerObj(x: Int, y: Int): StageObj = StageObj(x,y,ansi.BG_RED+ansi.FG_WHITE+"PL"+ansi.RESET);
      fun getCrateObj(x: Int, y: Int): StageObj  = StageObj(x,y,ansi.BG_YELLOW+ansi.FG_WHITE+"CR"+ansi.RESET);
    }
  }


  lateinit var stage:Stage;
  var maxLength:Int = 0;
  var cellStr:Array<String> = arrayOf(
      "  ",                         // empty
      ansi.BG_BLUE+"++"+ansi.RESET, // wall
      "DS"                          // destination
  );

  var moveX: Int = 0;
  var moveY: Int = 0;
  var moveCrate: Int = 0;

  fun printAllCells() {
    ansi.clearScreen();
    for (y in 0 until stage.y){
      for (x in 0 until stage.x){
        printCell(x,y);
      }
    }
    for (o in stage.stageObjs){
      printObj(o);
    }
  }

  fun setStageInfo(stageNum:Int) {
    // stage objects
    // 0 : empty
    // 1 : wall
    // 2 : destination
    if (stageNum==1) {
      stage = Stage(
        arrayOf(
          arrayOf(1, 1, 1, 1, 1, 1),
          arrayOf(1, 1, 1, 0, 0, 1),
          arrayOf(1, 1, 1, 0, 0, 1),
          arrayOf(1, 0, 0, 2, 0, 1),
          arrayOf(1, 0, 0, 0, 0, 1),
          arrayOf(1, 1, 1, 0, 2, 1),
          arrayOf(1, 1, 1, 0, 0, 1),
          arrayOf(1, 1, 1, 1, 1, 1)
        ),
        arrayOf(
            Stage.getPlayerObj(3,4),
            Stage.getCrateObj(2,3),
            Stage.getCrateObj(3,5)
        )
      );
    }
  }

  fun isCleared(): Boolean {
    for (y in 0 until stage.y){
      for (x in 0 until stage.x){
        if(stage.map[y][x] == 2){
          if(stage.findCrate(x,y) == -1) return false;
        }
      }
    }
    return true;
  }

  fun printCell(x:Int, y:Int) {
    setLocation(x,y);
    print(cellStr[stage.map[y][x]]);
  }

  fun printObj(o: Stage.StageObj) {
    setLocation(o.x,o.y);
    print(o.dispStr);
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

  fun canMove(o: Stage.StageObj, isPlayer: Boolean): Boolean {
    val toX = o.x + moveX;
    val toY = o.y + moveY;
    if ( toX < 0 || toX >= stage.x) return false;
    if ( toY < 0 || toY >= stage.y) return false;
    if (stage.map[toY][toX]==1)     return false;
    val crate = stage.findCrate(toX, toY)
    if (crate != -1){
      if(!isPlayer) return false;
      else{
        moveCrate = crate;
        return canMove(stage.stageObjs[crate],false);
      }
    }
    return true;
  }

  fun movePlayer(){
    if(canMove(stage.player,true)){
      if(moveCrate != 0){
        var crate = stage.stageObjs[moveCrate];
        crate.x += moveX;
        crate.y += moveY;
      }
      stage.player.x += moveX;
      stage.player.y += moveY;
    }
    moveX = 0;
    moveY = 0;
    moveCrate = 0;
  }
}
