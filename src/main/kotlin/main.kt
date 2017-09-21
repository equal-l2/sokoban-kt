package main
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import sokoban.Sokoban

/* ktlint-disable no-multi-spaces */

object GUIInterface : ApplicationAdapter() {
    enum class Direction {
        UP,
        DOWN,
        RIGHT,
        LEFT
    }
    const val tilePixel = 32 // Tiles are square
    const val windowPixel = 480 // Window is square
    lateinit var camera: OrthographicCamera
    lateinit var map: TiledMap
    lateinit var nextButton: Button
    lateinit var prevButton: Button
    lateinit var resetButton: Button
    lateinit var renderer: OrthogonalTiledMapRenderer
    lateinit var stage: Stage
    lateinit var tiles: Array<Array<TextureRegion>>
    var engine = Sokoban()
    var stageNum = 2
    var playerDirection = Direction.UP

    fun readStage(): Boolean {
        val stageStr = { n: Int -> "data/map/$n.map" }
        try {
            engine.readStage(stageStr(stageNum))
            updateMap()
            nextButton.setDisabled(
                    stageNum == Int.MAX_VALUE || !java.io.File(stageStr(stageNum+1)).exists()
            )
            prevButton.setDisabled(
                    stageNum == 1 || !java.io.File(stageStr(stageNum-1)).exists()
            )
            return true
        } catch (e: java.io.FileNotFoundException) {
            return false
        }
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
        renderer = OrthogonalTiledMapRenderer(map)
        camera.position.set(Vector3(
            0.5f*engine.stage.x*tilePixel,
            0.5f*engine.stage.y*tilePixel,
            0f
        ))
    }

    fun makeButton(name: String): Button {
        var style = Button.ButtonStyle()
        style.down = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_down.png"))))
        style.up = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_up.png"))))
        style.disabled = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_disabled.png"))))
        return Button(style)
    }

    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, 30f, 30f)
        camera.translate(-5f, -2f)
        camera.update()
        tiles = TextureRegion.split(Texture(Gdx.files.internal("data/tile.png")), tilePixel, tilePixel)

        // Prepare buttons
        resetButton = makeButton("reset")
        resetButton.addListener(
            object : ChangeListener() {
                override fun changed(e: ChangeEvent, a: Actor) { readStage() }
            }
        )

        prevButton = makeButton("prev")
        prevButton.addListener(
            object : ChangeListener() {
                override fun changed(e: ChangeEvent, a: Actor) { stageNum -= 1; readStage() }
            }
        )

        nextButton = makeButton("next")
        nextButton.addListener(
            object : ChangeListener() {
                override fun changed(e: ChangeEvent, a: Actor) { stageNum += 1; readStage() }
            }
        )

        // Prepare table
        var table = Table().left().top()
        table.add(resetButton).pad(10f)
        table.add(prevButton).pad(10f)
        table.add(nextButton).pad(10f)
        table.setFillParent(true)

        // Prepare stage
        stage = Stage()
        stage.addActor(table)

        Gdx.input.setInputProcessor(InputMultiplexer(
            stage,
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
        ))

        // Prepare camera
        camera = OrthographicCamera()
        camera.setToOrtho(false, 1f*windowPixel, 1f*windowPixel)

        // Read stage
        // Tile, Buttons, and camera are needed for readStage()
        readStage()

        camera.update()
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
        camera.update()
        renderer.setView(camera)
        renderer.render()
        stage.draw()
    }
}

fun main(args: Array<String>) {
    var config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Sokoban")
    config.setWindowedMode(480, 480)
    config.setResizable(false)
    Lwjgl3Application(GUIInterface, config)
}
