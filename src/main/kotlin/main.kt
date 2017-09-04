package main
/* ktlint-disable no-multi-spaces */

object CUIInterface {
    var engine = sokoban.Sokoban()
    var stageNum = 1
    var cellStr = arrayOf(
            //(background,string)
            arrayOf("",              "  "), // empty
            arrayOf(ansi.BG_GREEN,   "DS"), // destination
            arrayOf(ansi.BG_BLUE,    "PL"), // player
            arrayOf(ansi.BG_CYAN,    "PL"), // player on dest.
            arrayOf(ansi.BG_RED,     "CR"), // crate
            arrayOf(ansi.BG_YELLOW,  "CR"), // crate on dest.
            arrayOf("", ""),
            arrayOf("", ""),
            arrayOf(ansi.BG_MAGENTA, "++")  // wall
    )

    fun setLocation(x: Int, y: Int) = ansi.locateCursor( x * 2 + 3, y + 2 )
    fun getCellStr(kind: Int) = cellStr[kind].reduce { x, y -> x+y } + ansi.RESET

    fun printCell(x: Int, y: Int) {
        setLocation(x, y)
        print(getCellStr(engine.stage.map[y][x]))
    }

    fun printAllCells() {
        ansi.clearScreen()
        for (y in 0 until engine.stage.y) {
            for (x in 0 until engine.stage.x) {
                printCell(x, y)
            }
        }
    }

    fun readStage(): Boolean {
        try {
            engine.readStage("data/map/$stageNum.map")
            return true
        } catch (e: java.io.FileNotFoundException) {
            return false
        }
    }

    fun nextStage() {
        if (stageNum == Int.MAX_VALUE) kotlin.system.exitProcess(0)
        stageNum += 1
        if (!readStage()) kotlin.system.exitProcess(0)
    }

    fun selectStage() {
        while (true) {
            print("Select stage number (Ctrl+D to abort) : ")
            val buf = readLine()
            if (buf == null) {
                println("EOF detected")
                kotlin.system.exitProcess(0)
            } else {
                val num = buf.toIntOrNull()
                if (num == null) {
                    println("Supplied number is not valid")
                } else {
                    stageNum = num
                    if (!readStage()) {
                        println("map/$stageNum.map was not found")
                    } else {
                        break
                    }
                }
            }
        }
    }

    fun getInputForMove() {
        var buf = ""
        ansi.locateCursor(1, 15)
        print("WASD: ")
        while (arrayOf("w", "a", "s", "d").none { x -> buf.equals(x, true) }) {
            buf = readLine() ?: ""
        }

        if      (buf.equals("w", true)) engine.moveY = -1
        else if (buf.equals("s", true)) engine.moveY =  1
        else if (buf.equals("a", true)) engine.moveX = -1
        else if (buf.equals("d", true)) engine.moveX =  1
    }

    fun run() {
        selectStage()
        while (true) {
            while (true) {
                printAllCells()
                if (engine.isCleared()) break
                getInputForMove()
                engine.movePlayer()
            }
            println("\nCongratulations!")
            nextStage()
            println("Press Enter Key to proceed")
            println("Press Ctrl+D to abort")
            if (readLine() == null) kotlin.system.exitProcess(0)
        }
    }
}

fun main(args: Array<String>) {
    CUIInterface.run()
}
