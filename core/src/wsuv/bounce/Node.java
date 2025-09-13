package wsuv.bounce;

public class Node {
    int x, y;
    boolean walkable;
    Node parent;

    public Node(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
        this.parent = null; // No parent at first
    }

    public String toString() {
        return "Node{x=" + x + ", y=" + y + ", walkable=" + walkable + "}";
    }
}
