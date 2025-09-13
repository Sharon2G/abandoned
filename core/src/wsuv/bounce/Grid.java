package wsuv.bounce;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import java.util.LinkedList;
import java.util.Queue;

public class Grid {
    public boolean[][] walkableGrid;
    public int width;
    public int height;
    public int GRID_CELL_SIZE = 512;

    public Grid() {
        width = 100;
        height = 275;

        walkableGrid = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walkableGrid[x][y] = true;
            }
        }

        for (Polygon polygon : Level1.collisionPolygons) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (isCellCollidingWithPolygon(x, y, polygon)) {
                        walkableGrid[x][y] = false;
                    }
                }
            }
        }
    }

    private boolean isCellCollidingWithPolygon(int cellX, int cellY, Polygon polygon) {
        float cellCenterX = cellX * GRID_CELL_SIZE + GRID_CELL_SIZE / 2;
        float cellCenterY = cellY * GRID_CELL_SIZE + GRID_CELL_SIZE / 2;

        return Intersector.isPointInPolygon(polygon.getTransformedVertices(), 0, polygon.getTransformedVertices().length, cellCenterX, cellCenterY);
    }

    public Node getNode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return new Node(x, y, walkableGrid[x][y]);
        }
        return null;
    }

    public LinkedList<Node> findPathBFS(Node start, Node goal) {
        if (start == null || goal == null) {
            return null;
        }

        int[] dx = {0, 1, 0, -1, 1, 1, -1, -1};
        int[] dy = {-1, 0, 1, 0, 1, -1, 1, -1};

        Queue<Node> openList = new LinkedList<>();
        boolean[][] visited = new boolean[width][height];

        openList.offer(start);
        visited[start.x][start.y] = true;
        start.parent = null;

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.x == goal.x && current.y == goal.y) {
                return constructPath(current);
            }

            for (int i = 0; i < 8; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height
                        && walkableGrid[nx][ny] && !visited[nx][ny]) {

                    Node neighbor = getNode(nx, ny);
                    neighbor.parent = current;
                    visited[nx][ny] = true;
                    openList.offer(neighbor);
                }
            }
        }
        return null;
    }


    private LinkedList<Node> constructPath(Node goal) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = goal;
        while (current != null) {
            path.addFirst(current);
            current = current.parent;
        }
        return path;
    }
}
