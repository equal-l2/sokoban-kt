package main
/* ktlint-disable no-multi-spaces */
/* ktlint-disable no-wildcard-imports */

fun main(args: Array<String>) {
    Sokoban.selectStage()
    while (true) {
        Sokoban.printAllCells()
        if (Sokoban.isCleared()) break
        Sokoban.getInputForMove()
        Sokoban.movePlayer()
    }
    println("\nCongratulations!")
}

object Sokoban {
    class Stage(var map: Array<Array<Int>>) {
        val x: Int
        val y: Int
        var playerX = 0
        var playerY = 0
        init {
            x = map[0].size
            y = map.size
            if (map.any { a -> a.size != x }) {
                throw IllegalArgumentException("y differs. map:${map.size} y:$y")
            }

            var cntDest   = 0
            var cntPlayer = 0
            var cntCrate  = 0
            for (yidx in 0 until y) {
                for (xidx in 0 until x) {
                    val obj = map[yidx][xidx]
                    if (obj and DEST   != 0) {
                        cntDest++
                    }
                    if (obj and PLAYER != 0) {
                        playerX = xidx
                        playerY = yidx
                        cntPlayer++
                    }
                    if (obj and CRATE  != 0) {
                        cntCrate++
                    }
                }
            }
            if (cntDest < 1) {
                throw IllegalArgumentException("Dest should be 1 at least. dest:$cntDest")
            }
            if (cntPlayer != 1) {
                throw IllegalArgumentException("Player should be only 1. player:$cntPlayer")
            }
            if (cntCrate < 1) {
                throw IllegalArgumentException("Crate should be 1 at least. crate:$cntCrate")
            }
            if (cntDest != cntCrate) {
                throw IllegalArgumentException("Dests and crates don't match. dest:$cntDest crate:$cntCrate")
            }
        }
        companion object {
            fun fromFile(fileName: String): Stage {
                val mapFile = java.io.File(fileName).readLines()
                return Stage(mapFile.map { y -> y.map { x -> Character.getNumericValue(x) }.toTypedArray() }.toTypedArray())
            }
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
            val EMPTY  = 0b0000
            val DEST   = 0b0001
            val PLAYER = 0b0010
            val CRATE  = 0b0100
            val WALL   = 0b1000
        }
    }

    lateinit var stage: Stage
    var stageNum: Int = 1;

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

    var moveX = 0
    var moveY = 0
    var moveCrate = false

    fun printAllCells() {
        ansi.clearScreen()
        for (y in 0 until stage.y) {
            for (x in 0 until stage.x) {
                printCell(x, y)
            }
        }
    }

    fun isCleared() = !stage.map.any { y -> y.any { x -> (x == Stage.CRATE) } }

    fun getCellStr(kind: Int) = cellStr[kind].reduce { x, y -> x+y } + ansi.RESET

    fun printCell(x: Int, y: Int) {
        setLocation(x, y)
        print(getCellStr(stage.map[y][x]))
    }

    fun setLocation(x: Int, y: Int) = ansi.locateCursor( x * 2 + 3, y + 2 )

    fun readStage() : Boolean {
        try {
            stage = Stage.fromFile("map/$stageNum.map")
            return true;
        } catch (e: java.io.FileNotFoundException) {
            println("map/$stageNum.map was not found")
            return false;
        }
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
                    if (readStage()) break
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

        if      (buf.equals("w", true)) moveY = -1
        else if (buf.equals("s", true)) moveY =  1
        else if (buf.equals("a", true)) moveX = -1
        else if (buf.equals("d", true)) moveX =  1
    }

    fun canMove(x: Int, y: Int, objIsPlayer: Boolean): Boolean {
        val toX = x + moveX
        val toY = y + moveY
        val toObj = stage.map[toY][toX]
        if (
                (toX < 0 || toX >= stage.x) || // out-of-bound
                (toY < 0 || toY >= stage.y) || // out-of-bound
                (toObj == Stage.WALL)          /* you are in rock */
        ) return false

        if (toObj and Stage.CRATE != 0) {
            if (objIsPlayer) {
                moveCrate = true
                return canMove(toX, toY, false)
            } else {
                return false
            }
        }
        return true
    }

    fun movePlayer() {
        val toX = stage.playerX + moveX
        val toY = stage.playerY + moveY
        if (canMove(stage.playerX, stage.playerY, true)) {
            if (moveCrate) {
                stage.map[toY][toX] = stage.map[toY][toX] and Stage.CRATE.inv()
                stage.map[toY + moveY][toX + moveX] = stage.map[toY + moveY][toX + moveX] or Stage.CRATE
            }
            stage.map[stage.playerY][stage.playerX] = stage.map[stage.playerY][stage.playerX] and Stage.PLAYER.inv()
            stage.map[toY][toX] = stage.map[toY][toX] or Stage.PLAYER
            stage.playerX = toX
            stage.playerY = toY
        }
        moveX = 0
        moveY = 0
        moveCrate = false
    }
}
