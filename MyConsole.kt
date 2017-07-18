object MyConsole {
  val ES           = "\u001b[";
  val CLEAR        = ES + "2J";
  val RESET        = ES + "0m";
  val BOLD         = ES + "1m";
  val UNDERLINE    = ES + "4m";
  val REVERSE      = ES + "7m";
  val NO_BOLD      = ES + "22m";
  val NO_UNDERLINE = ES + "24m";
  val NO_REVERSE   = ES + "27m";
  val FG_BLACK     = ES + "30m";
  val FG_RED       = ES + "31m";
  val FG_GREEN     = ES + "32m";
  val FG_YELLOW    = ES + "33m";
  val FG_BLUE      = ES + "34m";
  val FG_MAGENDA   = ES + "35m";
  val FG_CYAN      = ES + "36m";
  val FG_WHITE     = ES + "37m";
  val FG_DEFAULT   = ES + "39m";
  val BG_BLACK     = ES + "40m";
  val BG_RED       = ES + "41m";
  val BG_GREEN     = ES + "42m";
  val BG_YELLOW    = ES + "43m";
  val BG_BLUE      = ES + "44m";
  val BG_MAGENDA   = ES + "45m";
  val BG_CYAN      = ES + "46m";
  val BG_WHITE     = ES + "47m";
  val BG_DEFAULT   = ES + "49m";

  fun clearScreen(){
    print(CLEAR);
    locateCursor(1,1);
  }

  fun moveCursor(x:Int, y:Int){
    if (y<0) upCursor(-y);   else downCursor(y);
    if (x<0) leftCursor(-x); else rightCursor(x);
  }

  fun upCursor(y:Int)            = print(ES + y + "A");
  fun downCursor(y:Int)          = print(ES + y + "B");
  fun rightCursor(x:Int)         = print(ES + x + "C");
  fun leftCursor(x:Int)          = print(ES + x + "D");
  fun locateCursor(x:Int, y:Int) = print(ES + y + ";" + x + "H");
}
