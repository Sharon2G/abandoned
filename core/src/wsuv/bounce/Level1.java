package wsuv.bounce;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Level1 extends ScreenAdapter {
    public static int port;
    private static Game game;
    public static TiledMap map;
    public static Player player;
    private OrthogonalTiledMapRenderer renderer;
    public static Array<Polygon> collisionPolygons;
    public static Array<Polygon> keyPolygons;
    public static Array<Polygon> powerPolygons;
    public static Array<Polygon> turretPolygons;
    public static Polygon exitPolygon;
    private HUD hud;

    public static OrthographicCamera camera;
    private int mapHeight = 140800;
    private int mapWidth = 51200;
    private float elapsedTime;
    private ShapeRenderer shapeRenderer;
    public static Client client;
    public static Server server;
    static List<Player> players = new ArrayList<Player>();
    static List<Enemy> enemies = new ArrayList<Enemy>();
    public static boolean canAttack = true;
    private float noAttactTimer = 0f;
    private float noAttactInterval = 30f;

    public Enemy enemy;
    private float elapsedTime2;
    private float elapsedTimeSlash;
    private float elapsedTimeSlash2;
    public static Grid grid;
    private boolean otherPlayerShot;
    private float shotTimer;
    private float nextShotInterval = 1f;
    public static int level;
    public static boolean isHost;


    public Level1(Game game, int portNum, boolean isHost, int level) {
        port = portNum;
        this.isHost = isHost;
        // Client-Server setup

        // if (!server && !clien)
        if (isHost) {
            server = new Server(game);
            server.start();
        }

        client = new Client(game, "localhost");
        client.start();

        if (server != null) {
            player = new Player(game, "Server", null, -1);
            Packet00Login loginPacket = new Packet00Login(player.username, player.getX(), player.getY());
            server.addConnection(player, loginPacket);

            loginPacket.writeData(client);
        }

        else {
            player = new Player(game, "Client", null, -1);
            Packet00Login loginPacket = new Packet00Login(player.username, player.getX(), player.getY());

            loginPacket.writeData(client);
        }

        players.add(player);
        hud = new HUD(game.am.get(game.RSC_MONO_FONT));
        this.level = level;
        this.game = game;

        setupMap();

        // Set up the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 28400, 17600);
        camera.update();

        shapeRenderer = new ShapeRenderer();

        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        hud.registerView("Lives:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(player.lives);
            }
        });

        hud.registerAction("level", new HUDActionCommand() {
            static final String help = "Usage: level <level_number>";
            @Override
            public String execute(String[] cmd) {
                try {
                    int levelNumber = Integer.parseInt(cmd[1]);
                    game.setScreen(new Level1(game, port + 1, isHost, levelNumber));
                    return "ok";
                } catch (Exception e) {
                    return help;
                }
            }

            @Override
            public String help(String[] cmd) {
                return help;
            }
        });
    }

    public static void handleShot(String username){

        game.setScreen(new Level1(game, port + 1, isHost, level));
    }


    public void setupMap() {
        TmxMapLoader mapLoader = new TmxMapLoader();
        if (level == 1)
            map = mapLoader.load("Map1.tmx");
        if (level == 2)
            map = mapLoader.load("Map2.tmx");
        if (level == 3)
            map = mapLoader.load("Map3.tmx");

        collisionPolygons = new Array<>();
        keyPolygons = new Array<>();
        powerPolygons = new Array<>();
        turretPolygons = new Array<>();
        renderer = new OrthogonalTiledMapRenderer(map, 1f / 512f);

        // Initialize the map objects
        MapObjects objects = map.getLayers().get("Objects").getObjects();
        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygon.setOrigin(0, 0); // Set origin
                polygon.setPosition(object.getProperties().get("x", Float.class),
                        object.getProperties().get("y", Float.class));
                collisionPolygons.add(polygon);
            }
        }

        objects = map.getLayers().get("Keys").getObjects();
        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygon.setOrigin(0, 0); // Set origin
                polygon.setPosition(object.getProperties().get("x", Float.class),
                        object.getProperties().get("y", Float.class));
                keyPolygons.add(polygon);
            }
        }

        objects = map.getLayers().get("Powers").getObjects();
        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygon.setOrigin(0, 0); // Set origin
                polygon.setPosition(object.getProperties().get("x", Float.class),
                        object.getProperties().get("y", Float.class));
                powerPolygons.add(polygon);
            }
        }

        objects = map.getLayers().get("Turrets").getObjects();
        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygon.setOrigin(0, 0); // Set origin
                polygon.setPosition(object.getProperties().get("x", Float.class),
                        object.getProperties().get("y", Float.class));
                turretPolygons.add(polygon);
            }
        }

        objects = map.getLayers().get("Door").getObjects();
        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygon.setOrigin(0, 0); // Set origin
                polygon.setPosition(object.getProperties().get("x", Float.class),
                        object.getProperties().get("y", Float.class));
                exitPolygon = polygon;
            }
        }

        grid = new Grid();

        enemy = new Enemy(player.username, grid, game, player);
        enemies.add(enemy);

        if (server != null) {
            server.addEnemy(enemy);
        }

    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;
        update(delta);
        ScreenUtils.clear(1, 1, 1, 1);
        renderer.setView(camera);
        renderer.render();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

//        if(players.size() >1) {
//            if (players.get(0).finishedGathering && players.get(1).finishedGathering) {
//                game.batch.draw(game.am.get(game.RSC_GAMEOVER_IMG, Texture.class), 800, 750);
//                System.exit(1);
////                if (level < 3)
////                    game.setScreen(new LoadScreen(game, level + 1));
////                else
////                    game.setScreen(new LoadScreen(game, 1));
//            }
//        }

        renderMap();
        //castRays();

//        Gdx.app.log("", ""+canAttack);
//        if (!canAttack)
//            game.batch.setColor(1, 1, 0, 1f);
//        else
//            game.batch.setColor(0, 0, 0, 1f);

        if (player.isSlashing) {
            elapsedTimeSlash += delta;
            TextureRegion slashFrame = (TextureRegion) player.slashAnimation.getKeyFrame(elapsedTimeSlash, false);

            if (player.slashAnimation.isAnimationFinished(elapsedTimeSlash)) {
                player.isSlashing = false;
                elapsedTimeSlash = 0; // Reset slashing time
            }

            if (player.mirrorLeft)
                game.batch.draw(slashFrame, player.getX() - 1000, player.getY() + 250f, 3000, 2000);
            else
                game.batch.draw(slashFrame, player.getX() + 1000, player.getY() + 250f, 3000, 2000);
        }
        TextureRegion playerFrame = (TextureRegion) player.currentAnimation.getKeyFrame(elapsedTime, true);
        if (player.mirrorLeft) {
            playerFrame.flip(true, false);
        }

        game.batch.draw(playerFrame, player.getX(), player.getY() + 250f, 3000, 2000);

        if (player.mirrorLeft) {
            playerFrame.flip(true, false);
        }

        TextureRegion enemyFrame = (TextureRegion) enemy.animation.getKeyFrame(elapsedTime, true);
        if (player.getX() <= enemy.getX()) {
            enemyFrame.flip(true, false);
        }
        game.batch.draw(enemyFrame, enemy.getX(), enemy.getY(), 1000, 1000);
        if (player.getX() <= enemy.getX()) {
            enemyFrame.flip(true, false);
        }
        for (Player player : players) {
            if (!player.username.equals(this.player.username)) {
                if (player.isSlashing) {
                    elapsedTimeSlash2 += delta;
                    TextureRegion slashFrame = (TextureRegion) player.slashAnimation.getKeyFrame(elapsedTimeSlash2, false);

                    if (player.slashAnimation.isAnimationFinished(elapsedTimeSlash2)) {
                        player.isSlashing = false;
                        elapsedTimeSlash2 = 0;
                    }

                    if (player.mirrorLeft)
                        game.batch.draw(slashFrame, player.getX() - 1000, player.getY() + 250f, 3000, 2000);
                    else
                        game.batch.draw(slashFrame, player.getX() + 1000, player.getY() + 250f, 3000, 2000);
                }

                elapsedTime2 += delta;
                playerFrame = (TextureRegion) player.currentAnimation.getKeyFrame(elapsedTime2, true);

                if (player.mirrorLeft) {
                    playerFrame.flip(true, false);
                }
                game.batch.draw(playerFrame, player.getX(), player.getY() + 250f, 3000, 2000);

                if (player.mirrorLeft) {
                    playerFrame.flip(true, false);
                }

                for (Enemy enemy : enemies) {
                    if (!enemy.username.equals(this.player.username)) {

                        enemyFrame = (TextureRegion) enemy.animation.getKeyFrame(elapsedTime2, true);
                        if (player.getX() <= enemy.getX()) {
                            enemyFrame.flip(true, false);
                        }
                        game.batch.draw(enemyFrame, enemy.getX(), enemy.getY(), 1000, 1000);
                        if (player.getX() <= enemy.getX()) {
                            enemyFrame.flip(true, false);
                        }
                    }
                }
            }
        }
        player.update(delta);
        enemy.update(delta);
        hud.draw(game.batch);
        game.batch.end();

        if (canAttack) {
            castRays();
            enemyCollision();
        }
    }

    private void enemyCollision() {
        for (Enemy enemy : enemies) {
            for (Player player : players) {
                Rectangle playerBounds = new Rectangle(
                        player.getX() + 5000 / 5,
                        player.getY() + 3000 / 4,
                        5000 / 6, 3000 / 4
                );
                Rectangle enemyBounds = new Rectangle(
                        enemy.getX(),
                        enemy.getY(),
                        1000, 1000
                );

                if (Intersector.overlaps(playerBounds, enemyBounds)) {
                    if (!otherPlayerShot && player != this.player) {
                        otherPlayerShot = true;
                    } else if (!this.player.isShot && player == this.player) {
                        player.isShot = true;
                        player.lives -= 1;
                    }
                }
            }
        }

    }

    public void castRays() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);

        boolean blocked = false;

        for (Polygon turret : turretPolygons) {
            for (Player player : players) {
                if (!otherPlayerShot && player != this.player) {


                    Vector2 playerPos = new Vector2(player.getX() + (5000 / 3.5f), player.getY() + (3000 / 2.5f));
                    Vector2 turretPos = new Vector2(turret.getX() + (turret.getBoundingRectangle().width / 2), turret.getY() + (turret.getBoundingRectangle().height / 2));
                    blocked = false;

                    for (Polygon polygon : collisionPolygons) {
                        if (Intersector.intersectSegmentPolygon(turretPos, playerPos, polygon)) {

                            blocked = true;
                        }
                    }
                    if (!blocked) {
                        shapeRenderer.rectLine(playerPos,turretPos,150);
                        otherPlayerShot = true;
                    }
                }
                else if (!this.player.isShot && player == this.player){

                    Vector2 playerPos = new Vector2(player.getX() + (5000 / 3.5f), player.getY() + (3000 / 2.5f));
                    Vector2 turretPos = new Vector2(turret.getX() + (turret.getBoundingRectangle().width / 2), turret.getY() + (turret.getBoundingRectangle().height / 2));
                    blocked = false;

                    for (Polygon polygon : collisionPolygons) {
                        if (Intersector.intersectSegmentPolygon(turretPos, playerPos, polygon)) {

                            blocked = true;
                        }
                    }
                    if (!blocked) {
                        shapeRenderer.rectLine(playerPos,turretPos,150);
                        player.isShot = true;
                        player.lives -= 1;
                    }
                }
            }
        }
        shapeRenderer.end();
    }

    public void update(float delta) {

        if (players.size() > 1) {
            if (players.get(0).finishedGathering && players.get(1).finishedGathering) {
                System.exit(0);
//                game.batch.draw(game.am.get(game.RSC_GAMEOVER_IMG, Texture.class), 800, 750);
//
//                Timer.schedule(new Timer.Task() {
//                    @Override
//                    public void run() {
//                        if (level < 3) {
//                            game.setScreen(new LoadScreen(game, level + 1));
//                        } else {
//                            game.setScreen(new LoadScreen(game, 1));
//                        }
//                    }
//                }, 2);
            }
        }
        if (otherPlayerShot) {
            shotTimer += delta;
            if (shotTimer >= nextShotInterval) {
                otherPlayerShot = false;
                shotTimer = 0f;
            }
        }

        if (!canAttack) {
            noAttactTimer += delta;
            if (noAttactTimer >= noAttactInterval) {
                canAttack = true;
                noAttactTimer = 0f;
            }
        }

        camera.position.x = player.positionTranslation(14200, true)-(camera.viewportWidth/2);
        camera.position.y = player.positionTranslation(8800, false)-(camera.viewportHeight/2);

        camera.position.x = MathUtils.clamp(camera.position.x, 14300, mapWidth - 100 - (camera.viewportWidth/2));
        camera.position.y = MathUtils.clamp(camera.position.y, 8800, mapHeight);

        camera.update();
    }

    public static void handleAnim(String username, String animName, String mirrorLeft) {
        for (Player player : players) {
            if (player.username.equals(username)) {

                if (mirrorLeft.equals("true"))
                    player.mirrorLeft = true;
                else
                    player.mirrorLeft = false;


                if (animName.equals("walk")) {
                    player.currentAnimation = player.walkAnimation;
                } else if (animName.equals("jump")) {
                    player.currentAnimation = player.jumpAnimation;
                } else {
                    player.currentAnimation = player.idleAnimation;
                }
            }
        }
    }

    public static void handleSlash(String username, String tileName) {
        for (Player player : players) {
            if (player.username.equals(username)) {
                player.isSlashing = true;
                player.destroyTile(tileName);
            }
        }
    }

    public static void addPlayer(Player player) {
        players.add(player);
    }

    public static void addEnemy(Enemy enemy) {
        boolean alreadyConnected = false;
        for (Enemy e : enemies) {
            if (e.username.equalsIgnoreCase(enemy.username)) {
                alreadyConnected = true;
            }
        }

        if (!alreadyConnected) {
            Enemy newEnemy = new Enemy(enemy.username, Level1.grid, game, Level1.player);
            enemies.add(newEnemy);
        }
    }

    public static void movePlayer(String username, float x, float y) {
        for (Player player : players) {
            if (player.username.equals(username)) {
                player.setX(x);
                player.setY(y);
            }
        }
    }

    public static void moveEnemy(String username, float x, float y) {
        for (Enemy enemy : enemies) {
            if (enemy.username.equals(username)) {
                enemy.setX(x);
                enemy.setY(y);
            }
        }
    }
    public void renderMap() {
        for (MapLayer layer : map.getLayers()) {
            MapObjects objects = layer.getObjects();
            for (MapObject object : objects) {
                if (object instanceof TiledMapTileMapObject) {
                    TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;
                    TextureRegion textureRegion = tileObject.getTile().getTextureRegion();

                    float x = tileObject.getProperties().get("x", Float.class);
                    float y = tileObject.getProperties().get("y", Float.class);


                    if (textureRegion != null) {
                        game.batch.draw(
                                textureRegion,
                                x,
                                y,
                                tileObject.getOriginX(),
                                tileObject.getOriginY(),
                                tileObject.getProperties().get("width", Float.class),
                                tileObject.getProperties().get("height", Float.class),
                                1,
                                1,
                                -tileObject.getRotation());
                    }
                }
            }
        }
    }
}


