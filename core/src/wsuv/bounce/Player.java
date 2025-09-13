package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.net.InetAddress;

public class Player extends Sprite {
    public float x, y;
    private Game game;
    public float velocityY;
    public float velocityX;
    private float gravity = -40000f;
    private float jumpForce = 17000f;
    private float maxSpeed = 6000f;
    private boolean isWallSliding = true;
    private float wallSlidingSpeed = 1000f;
    private boolean isGrounded = true;
    public boolean mirrorLeft;
    public InetAddress ipAddress;
    public int port;
    public String username;
    public float elapsedTime;
    private boolean oneMoreJump = false;
    public boolean isSlashing = false;
    private boolean slashSent = false;
    private float nextShotInterval = 1f;
    private float shotTimer = 0f;
    public boolean isShot = false;
    public int lives = 20;
    public int numKeys = 0;
    public boolean finishedGathering = false;



    public Animation<TextureRegion> walkAnimation;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> jumpAnimation;
    public Animation<TextureRegion> currentAnimation;
    public Animation<TextureRegion> slashAnimation;


    public Player(Game game, String username, InetAddress ipAddress, int port) {
        this.game = game;
        setPosition(12,12);
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username;

        initializePlayer();

    }

    public void initializePlayer() {
        x = 1000;
        y = 1200;
        velocityY = 0;
        velocityX = 0;
        this.isGrounded = false;

        Array<TextureRegion> walkFrames = new Array<>();
        for (int i = 0; i <= 19; i++) {
            String fileName = String.format(Game.RSC_WALK_TEXTURE, i);
            walkFrames.add(new TextureRegion(game.am.get(fileName, Texture.class)));
        }
        walkAnimation = new Animation<>(0.1f, walkFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> slashFrames = new Array<>();
        for (int i = 1; i <= 10; i++) {
            String fileName = String.format(Game.RSC_SLASH_TEXTURE, i);
            slashFrames.add(new TextureRegion(game.am.get(fileName, Texture.class)));
        }
        slashAnimation = new Animation<>(0.1f, slashFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 19; i++) {
            String fileName = String.format(Game.RSC_IDLE_TEXTURE, i);
            idleFrames.add(new TextureRegion(game.am.get(fileName, Texture.class)));
        }
        idleAnimation = new Animation<>(0.1f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i <= 7; i++) {
            String fileName = String.format(Game.RSC_JUMP_TEXTURE, i);
            jumpFrames.add(new TextureRegion(game.am.get(fileName, Texture.class)));
        }
        jumpAnimation = new Animation<>(0.1f, jumpFrames, Animation.PlayMode.LOOP);

        currentAnimation = idleAnimation;
    }
    public float positionTranslation(float startc, boolean isx){
        if (isx)return x+startc+970;
        else return y+startc-1167;
    }

    private boolean checkCollisions(float deltaX, float deltaY) {
        Rectangle bounds = new Rectangle(
                getX() + 5000 / 5 + deltaX,
                getY() + 3000 / 4 + deltaY,
                5000 / 6, 3000 / 4
        );
        for (Polygon polygon : Level1.collisionPolygons) {
            if (Intersector.overlaps(bounds, polygon.getBoundingRectangle())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSideCollisions(float deltaX, float deltaY) {
        Rectangle bounds = new Rectangle(
                getX() + 5000/5 + deltaX,
                getY() + 3000/4,
                10, 3000 / 3
        );
        for (Polygon polygon : Level1.collisionPolygons) {
            if (Intersector.overlaps(bounds, polygon.getBoundingRectangle())) {
                return true;
            }
        }
        return false;
    }

    public void update(float delta) {
        if (isShot) {
            shotTimer += delta;
            if (shotTimer >= nextShotInterval) {
                isShot = false;
//                Packet05Shot shotPacket = new Packet05Shot(username);
//                shotPacket.writeData(Level1.client);
                shotTimer = 0f;
            }
        }

//        if (lives == 0) {
//            Packet05Shot shotPacket = new Packet05Shot(username);
//            shotPacket.writeData(Level1.client);
//        }

        elapsedTime += delta;
        boolean collisionX = false;
        boolean collisionY = false;

        currentAnimation = idleAnimation;
        isGrounded = checkCollisions(0, -10f);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            mirrorLeft = true;
            if (x <= -970) {
                velocityX = 0;
                x = -965;
            } else if (velocityX > -maxSpeed) {
                velocityX -= maxSpeed;
            }

            collisionX = checkCollisions(velocityX * delta, 70);
            if (!collisionX) {
                x += velocityX * delta;
            } else {
                velocityX = 0;
            }
            if (isGrounded)
                currentAnimation = walkAnimation;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            mirrorLeft = false;
            if (velocityX < maxSpeed) {
                if (velocityX < 0) {
                    velocityX = 0;
                }
                velocityX += maxSpeed;
            }
            collisionX = checkCollisions(velocityX * delta, 70);
            if (!collisionX) {
                x += velocityX * delta;
            } else {
                velocityX = 0;
            }
            if (isGrounded)
                currentAnimation = walkAnimation;
        } else {
            velocityX = 0;
        }

        boolean onLeftWall = checkSideCollisions(-100, 0);
        boolean onRightWall = checkSideCollisions(+5000 / 6 + 100, 0);
        //Gdx.app.log("Debug", "isGrounded: " + isGrounded + ", isWallSliding: " + isWallSliding + ", oneMoreJump: " + oneMoreJump);

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (isGrounded) {
                oneMoreJump = true;
                velocityY = jumpForce;
                isGrounded = false;
                currentAnimation = jumpAnimation;
            } else if (isWallSliding) {
                velocityY = jumpForce;
            } else if (oneMoreJump) {
                oneMoreJump = false;
                velocityY = jumpForce;
            }
        } else {
            isWallSliding = false;
            if (onRightWall || onLeftWall) {
                if (velocityY < 0) {
                    currentAnimation = jumpAnimation;
                    isWallSliding = true;
                    velocityY = MathUtils.clamp(velocityY, -wallSlidingSpeed, Float.MAX_VALUE);
                    collisionY = checkCollisions(0, velocityY * delta);
                    if (!collisionY) {
                        collisionY = checkCollisions(0, velocityY * delta);
                        if (!collisionY)
                            y += velocityY * delta;
                    }
                }
            }
        }

        if (!isGrounded) {
            velocityY += gravity * delta;
            collisionY = checkCollisions(0, velocityY * delta);
            if (!collisionY) {
                y += velocityY * delta;
            } else {
                velocityY = 0;
            }
        }

        setPosition(x, y);
        Packet01Move movePacket = new Packet01Move(username, getX(), getY());
        movePacket.writeData(Level1.client);

        String animation;
        Packet02Animation animPacket;
        if (currentAnimation == walkAnimation)
            animation = "walk";
        else if (currentAnimation == idleAnimation)
            animation = "idle";
        else
            animation = "jump";
        if (mirrorLeft == true)
            animPacket = new Packet02Animation(username, animation, "true");
        else
            animPacket = new Packet02Animation(username, animation, "false");
        animPacket.writeData(Level1.client);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            isSlashing = true;
            Rectangle slashBounds = new Rectangle( getX() + 1000 + 800, getY() + 3000/4, 1800, 3000/3);
            if (mirrorLeft)
                slashBounds = new Rectangle( getX() - 1000 + 400, getY() + 3000/4, 1800, 3000/3);

            if (numKeys < 2) {
                for (Polygon polygon : Level1.keyPolygons) {
                    if (Intersector.overlaps(slashBounds, polygon.getBoundingRectangle())) {
                        MapLayer layer = Level1.map.getLayers().get("Keys");
                        MapObjects objects = layer.getObjects();
                        for (MapObject object : objects) {
                            if (object instanceof PolygonMapObject) {
                                if (((PolygonMapObject) object).getPolygon() == polygon) {
                                    Level1.keyPolygons.removeValue(polygon, true);
                                    MapLayer tileLayer = Level1.map.getLayers().get("Layer 0");
                                    MapObjects tileObjects = tileLayer.getObjects();
                                    for (MapObject tileObject : tileObjects) {
                                        if (tileObject instanceof TiledMapTileMapObject) {
                                            if (tileObject.getProperties().containsKey("Name")) {
                                                if (tileObject.getProperties().get("Name", String.class).equals(object.getProperties().get("Name", String.class)))
                                                    tileObjects.remove(tileObject);
                                            }
                                        }
                                    }
                                    slashSent = true;
                                    numKeys += 1;

                                    if (numKeys == 2) {
                                        finishedGathering = true;
                                        // Sent package to signal that gathering is over.
                                    }

                                    Packet03Slash slashPacket = new Packet03Slash(username, object.getProperties().get("Name", String.class));
                                    slashPacket.writeData(Level1.client);
                                    objects.remove(object);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!slashSent) {
                for (Polygon polygon : Level1.powerPolygons) {
                    if (Intersector.overlaps(slashBounds, polygon.getBoundingRectangle())) {
                        MapLayer layer = Level1.map.getLayers().get("Powers");
                        MapObjects objects = layer.getObjects();
                        for (MapObject object : objects) {
                            if (object instanceof PolygonMapObject) {
                                if(((PolygonMapObject) object).getPolygon() == polygon) {
                                    Level1.powerPolygons.removeValue(polygon, true);
                                    MapLayer tileLayer = Level1.map.getLayers().get("Layer 0");
                                    MapObjects tileObjects = tileLayer.getObjects();
                                    for (MapObject tileObject : tileObjects) {
                                        if (tileObject instanceof TiledMapTileMapObject) {
                                            if (tileObject.getProperties().containsKey("Name")) {
                                                if (tileObject.getProperties().get("Name", String.class).equals(object.getProperties().get("Name", String.class)))
                                                    tileObjects.remove(tileObject);
                                            }
                                        }
                                    }
                                    slashSent = true;
                                    Level1.canAttack = false;
                                    Packet03Slash slashPacket = new Packet03Slash(username, object.getProperties().get("Name", String.class));
                                    slashPacket.writeData(Level1.client);
                                    objects.remove(object);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!slashSent) {
                Packet03Slash slashPacket = new Packet03Slash(username, "None");
                slashPacket.writeData(Level1.client);
            }
            slashSent = false;
        }
    }

    public void destroyTile(String tileName) {
        if (!tileName.equals("None")) {
            boolean tileFound = false;
            MapLayer layer = Level1.map.getLayers().get("Keys");
            MapObjects objects = layer.getObjects();
            for (MapObject object : objects) {
                if (object instanceof PolygonMapObject) {
                    if (object.getProperties().get("Name", String.class).equals(tileName)) {
                        Level1.keyPolygons.removeValue(((PolygonMapObject) object).getPolygon(), true);
                        MapLayer tileLayer = Level1.map.getLayers().get("Layer 0");
                        MapObjects tileObjects = tileLayer.getObjects();
                        for (MapObject tileObject : tileObjects) {
                            if (tileObject instanceof TiledMapTileMapObject) {
                                if (tileObject.getProperties().containsKey("Name")) {
                                    if (tileObject.getProperties().get("Name", String.class).equals(tileName)) {
                                        tileObjects.remove(tileObject);
                                        objects.remove(object);
                                        tileFound = true;

                                        numKeys += 1;
                                        if (numKeys == 2)
                                            finishedGathering = true;
                                    }
                                }
                            }
                        }

                        break;
                    }
                }
            }

            if (tileFound)
                return;

            layer = Level1.map.getLayers().get("Powers");
            objects = layer.getObjects();
            for (MapObject object : objects) {
                if (object instanceof PolygonMapObject) {
                    if (object.getProperties().get("Name", String.class).equals(tileName)) {
                        Level1.keyPolygons.removeValue(((PolygonMapObject) object).getPolygon(), true);
                        MapLayer tileLayer = Level1.map.getLayers().get("Layer 0");
                        MapObjects tileObjects = tileLayer.getObjects();
                        for (MapObject tileObject : tileObjects) {
                            if (tileObject instanceof TiledMapTileMapObject) {
                                if (tileObject.getProperties().containsKey("Name")) {
                                    if (tileObject.getProperties().get("Name", String.class).equals(tileName)) {
                                        tileObjects.remove(tileObject);
                                        objects.remove(object);
                                        Level1.canAttack = false;
                                    }
                                }
                            }
                        }

                        break;
                    }
                }
            }
        }
    }
}
