package main
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import sokoban.Sokoban

/* ktlint-disable no-multi-spaces */

object GUIInterface : ApplicationAdapter() {
    enum class Direction {
        UP,
        DOWN,
        RIGHT,
        LEFT
    }
    val tilePixel = 32
    lateinit var map: TiledMap
    lateinit var renderer: OrthogonalTiledMapRenderer
    lateinit var camera: OrthographicCamera
    lateinit var tiles: Array<Array<TextureRegion>>
    var engine = Sokoban()
    var stageNum = 1
    var playerDirection = Direction.UP

    fun readStage(): Boolean {
        try {
            engine.readStage("data/map/$stageNum.map")
            updateMap()
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

    fun updateMap() {
        var layer = TiledMapTileLayer(engine.stage.x, engine.stage.y, tilePixel, tilePixel)
        for (y in 0 until engine.stage.y) {
            for (x in 0 until engine.stage.x) {
                val cell = TiledMapTileLayer.Cell().setTile(StaticTiledMapTile(
                        when (engine.stage.map[y][x]) {
                            Sokoban.Stage.EMPTY
                                -> tiles[0][4]
                            Sokoban.Stage.DEST
                                -> tiles[0][2]
                            Sokoban.Stage.PLAYER,
                            (Sokoban.Stage.DEST or Sokoban.Stage.PLAYER)
                                -> when (playerDirection) {
                                    Direction.UP    -> tiles[0][5]
                                    Direction.RIGHT -> tiles[0][6]
                                    Direction.DOWN  -> tiles[0][7]
                                    Direction.LEFT  -> tiles[0][8]
                                }
                            Sokoban.Stage.CRATE
                                -> tiles[0][0]
                            (Sokoban.Stage.DEST or Sokoban.Stage.CRATE)
                                -> tiles[0][1]
                            Sokoban.Stage.WALL
                                -> tiles[0][9]
                            else
                                -> tiles[0][3]
                        }
                ))
                layer.setCell(x, engine.stage.y-1-y, cell)
            }
        }
        map = TiledMap()
        map.getLayers().add(layer)
        renderer = OrthogonalTiledMapRenderer(map, 1/10f)
    }

    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, 30f, 30f)
        camera.translate(-5f, -2f)
        camera.update()
        tiles = TextureRegion.split(Texture(Gdx.files.internal("data/tile.png")), tilePixel, tilePixel)
        Gdx.input.setInputProcessor(
                object : InputAdapter () {
                    override fun keyDown(keyCode: Int) : Boolean {
                        when (keyCode) {
                            Input.Keys.LEFT  -> {
                                engine.moveX = -1
                                playerDirection = Direction.LEFT
                            }
                            Input.Keys.RIGHT -> {
                                engine.moveX = 1
                                playerDirection = Direction.RIGHT
                            }
                            Input.Keys.UP    -> {
                                engine.moveY = -1
                                playerDirection = Direction.UP
                            }
                            Input.Keys.DOWN  -> {
                                engine.moveY = 1
                                playerDirection = Direction.DOWN
                            }
                            else -> return true
                        }

                        engine.movePlayer()
                        updateMap()
                        return true
                    }
                }
        )
        readStage()
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
        camera.update()
        renderer.setView(camera)
        renderer.render()
    }
}

fun main(args: Array<String>) {
    var config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Sokoban")
    config.setWindowedMode(480, 480)
    config.setResizable(false)
    Lwjgl3Application(GUIInterface, config)
}
