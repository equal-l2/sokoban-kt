package sokoban
/* ktlint-disable no-multi-spaces */
/* ktlint-disable no-wildcard-imports */

class Sokoban {
    class Stage(var map: Array<Array<Int>>) {
        val x: Int
        val y: Int
        var playerX = 0
        var playerY = 0
        init {
            x = map[0].size
            y = map.size
            if (map.any { it.size != x }) {
                throw IllegalArgumentException("x differs. map:${map.size} x:$x")
            }

            /* count dests, player, and crates */
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

            /* verify stage */
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
            // Construct Stage object from file
            fun fromFile(fileName: String): Stage {
                val mapFile = java.io.File(fileName).readLines()
                return Stage(mapFile.map { it.map { Character.getNumericValue(it) }.toTypedArray() }.toTypedArray())
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

    var moveX = 0
    var moveY = 0
    var moveCrate = false

    fun isCleared() = !stage.map.any { it.any { it == Stage.CRATE } }

    fun readStage(fileName: String) {
        stage = Stage.fromFile(fileName)
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
