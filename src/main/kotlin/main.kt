package main
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Color
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
    lateinit var congrats: Label
    lateinit var renderer: OrthogonalTiledMapRenderer
    lateinit var stage: Stage
    lateinit var tiles: Array<Array<TextureRegion>>
    var engine = Sokoban()
    var stageNum = 1
    var playerDirection = Direction.UP

    // Read stage and set buttons' states
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

            congrats.setVisible(engine.isCleared())
            return true
        } catch (e: java.io.FileNotFoundException) {
            return false
        }
    }

    // Generate map instance from engine's stage data
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
        map = TiledMap().apply { getLayers().add(layer) }
        renderer = OrthogonalTiledMapRenderer(map)
        camera.apply {
            position.set(Vector3(
                0.5f*engine.stage.x*tilePixel,
                0.5f*engine.stage.y*tilePixel,
                0f
            ))
            update()
        }
    }

    // Utility function for making a button
    fun makeButton(name: String): Button {
        return Button(
            Button.ButtonStyle().apply {
                down = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_down.png"))))
                up = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_up.png"))))
                disabled = SpriteDrawable(Sprite(Texture(Gdx.files.internal("data/button/${name}_disabled.png"))))
            }
        )
    }

    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, 30f, 30f)
        camera.translate(-5f, -2f)
        camera.update()
        tiles = TextureRegion.split(Texture(Gdx.files.internal("data/tile.png")), tilePixel, tilePixel)

        // Prepare buttons
        resetButton = makeButton("reset").apply {
            addListener(
                object : ChangeListener() {
                    override fun changed(e: ChangeEvent, a: Actor) { readStage() }
                }
            )
        }

        prevButton = makeButton("prev").apply {
            addListener(
                object : ChangeListener() {
                    override fun changed(e: ChangeEvent, a: Actor) { stageNum -= 1; readStage() }
                }
            )
        }

        nextButton = makeButton("next").apply {
            addListener(
                object : ChangeListener() {
                    override fun changed(e: ChangeEvent, a: Actor) { stageNum += 1; readStage() }
                }
            )
        }

        // Prepare congrats message
        congrats = Label(
            "Congratulations!\n<= Click here to proceed to the next stage.",
            Label.LabelStyle(BitmapFont(), Color.FIREBRICK as Color)
        ).apply { setVisible(false) }

        // Prepare stage
        stage = Stage().apply {
            addActor(
                // Set layout
                Table().left().top().apply {
                    add(resetButton).pad(10f)
                    add(prevButton).pad(10f)
                    add(nextButton).pad(10f)
                    add(congrats).pad(10f)
                    setFillParent(true)
                }
            )
        }

        // Prepare input processor
        // It must be multiplexed to handle both buttons and keys
        Gdx.input.setInputProcessor(InputMultiplexer(
            stage,
            object : InputAdapter () { // key handler
                override fun keyDown(keyCode: Int) : Boolean {
                    if (engine.isCleared()) return true
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
                    congrats.setVisible(engine.isCleared())
                    return true
                }
            }
        ))

        // Prepare camera
        camera = OrthographicCamera().apply {
            setToOrtho(false, 1f*windowPixel, 1f*windowPixel)
        }

        // Tile, Buttons, and camera are needed for readStage()
        readStage()
    }

    override fun render() {
        // Set background color
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)

        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
        camera.update()
        renderer.setView(camera)
        renderer.render()
        stage.draw()
    }
}

fun main(args: Array<String>) {
    var config = LwjglApplicationConfiguration().apply {
        title = "Sokoban"
        width = 480
        height = 480
        resizable = false
        allowSoftwareMode = true
    }
    LwjglApplication(GUIInterface, config)
}
