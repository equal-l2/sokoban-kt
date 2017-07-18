object MyConsole {
  val ES           = "\u001b[";
  val CLEAR        = ES + "2J";
  val RESET        = ES + "m";
  val TEXT_BLACK   = ES + "30m";
  val TEXT_RED     = ES + "31m";
  val TEXT_GREEN   = ES + "32m";
  val TEXT_YELLOW  = ES + "33m";
  val TEXT_BLUE    = ES + "34m";
  val TEXT_MAGENDA = ES + "35m";
  val TEXT_CYAN    = ES + "36m";
  val TEXT_WHITE   = ES + "37m";
  val BG_BLACK     = ES + "40m";
  val BG_RED       = ES + "41m";
  val BG_GREEN     = ES + "42m";
  val BG_YELLOW    = ES + "43m";
  val BG_BLUE      = ES + "44m";
  val BG_MAGENDA   = ES + "45m";
  val BG_CYAN      = ES + "46m";
  val BG_WHITE     = ES + "47m";

  fun setColor(com:String) = print(com);

  fun resetColor() = print(RESET);

  fun clearScreen(){
    print(CLEAR);
    locateCursor(1,1);
  }

  fun moveCursor(x:Int, y:Int){
    if (y<0) upCursor(-y);   else downCursor(y);
    if (x<0) leftCursor(-x); else rightCursor(x);
  }

  fun upCursor(y:Int)    = print(ES + y + "A");

  fun downCursor(y:Int)  = print(ES + y + "B");

  fun rightCursor(x:Int) = print(ES + x + "C");

  fun leftCursor(x:Int)  = print(ES + x + "D");

  fun locateCursor(x:Int, y:Int) = print(ES + y + ";" + x + "H");

  fun inputKeyboard():String = readLine()?:"";

}
