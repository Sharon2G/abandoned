package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.LinkedList;
import java.util.List;
public class Enemy extends Sprite {
    public float speed = 9000;
    private TextureRegion[] frames;
    public Animation<TextureRegion> animation;
    public boolean isDead = false;
    private float deathDuration = 8f;
    private float deathTimer = 0f;
    public boolean active = true;
    public boolean mirrorLeft;
    private Player target;
    private Grid grid;
    public String username;
    private LinkedList<Node> path;
    public Node startNode = null;
    public Node endNode = null;

    private float pathUpdateInterval = 0.3f;
    private float pathUpdateTimer = 0f;

    public Enemy(String username, Grid grid, Game game, Player target) {
        this.username = username;
        setPosition(2100, 2100);
        this.grid = grid;
        this.target = target;

        frames = new TextureRegion[4];
        frames[0] = new TextureRegion(game.am.get(game.RSC_BAT1_IMG, Texture.class), 0, 0, 93, 63);
        frames[1] = new TextureRegion(game.am.get(game.RSC_BAT2_IMG, Texture.class), 0, 0, 120, 69);
        frames[2] = new TextureRegion(game.am.get(game.RSC_BAT3_IMG, Texture.class), 0, 0, 93, 63);
        frames[3] = new TextureRegion(game.am.get(game.RSC_BAT4_IMG, Texture.class), 0, 0, 69, 93);

        setSize(1000, 1000);
        animation = new Animation<>(1f / 10f, frames);
    }

    public void update(float delta) {
        if (isDead) {
            deathTimer += delta;
            if (deathTimer >= deathDuration) {
                isDead = false;
                deathTimer = 0f;
                pathUpdateTimer += pathUpdateInterval;
            }
            return;
        }

        if (!active) return;

        pathUpdateTimer += delta;
        if (pathUpdateTimer >= pathUpdateInterval) {
            path = null;
            pathUpdateTimer = 0f;

            startNode = grid.getNode((int) ((getX() + 500) / 512), (int) ((getY() + 500) / 512));
            endNode = grid.getNode((int) ((target.getX() + 5000 / 5 + 250) / 512), (int) ((target.getY() + 3000 / 3) / 512));

            path = grid.findPathBFS(startNode, endNode);
            if (path == null)
                return;
            path.remove(0);
        }

        if (path != null && !path.isEmpty()) {
            Node targetNode = path.get(0);
            Vector2 targetPosition = new Vector2(targetNode.x * 512, targetNode.y * 512);

            float moveDistance = speed * (delta/2);

            float newX = getX();
            if (newX < targetPosition.x) {
                newX += moveDistance;
                if (newX > targetPosition.x) newX = targetPosition.x;
            } else if (newX > targetPosition.x) {
                newX -= moveDistance;
                if (newX < targetPosition.x) newX = targetPosition.x;
            }

            float newY = getY();
            if (newY < targetPosition.y) {
                newY += moveDistance;
                if (newY > targetPosition.y) newY = targetPosition.y;
            } else if (newY > targetPosition.y) {
                newY -= moveDistance;
                if (newY < targetPosition.y) newY = targetPosition.y;
            }

            setPosition(newX, newY);

            if (newX == targetPosition.x && newY == targetPosition.y) {
                path.remove(0);
            }


            mirrorLeft = target.mirrorLeft;
        }
        Packet04Enemy enemyPacket = new Packet04Enemy(username, getX(), getY());
        enemyPacket.writeData(Level1.client);
    }
}
