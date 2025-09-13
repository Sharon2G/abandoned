package wsuv.bounce;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static wsuv.bounce.Level1.port;


public class Server extends Thread {
    private Game game;
    public static DatagramSocket socket;
    private List<Player> connectedPlayers = new ArrayList<Player>();
    private List<Enemy> connectedEnemies = new ArrayList<Enemy>();

    public Server (Game game) {
        this.game = game;
        while (socket == null) {
            try {
                socket = new DatagramSocket(port);
                port = socket.getLocalPort();  // Get the local port if successfully bound
                System.out.println("Socket bound to port: " + port);

                socket.setSoTimeout(0);
                socket.setReuseAddress(true);

            } catch (SocketException e) {
                System.err.println("Failed to bind to port " + port + ": " + e.getMessage());
                port++;
                System.out.println("Retrying with port " + port + "...");
            }
        }
    }

    public void run() {

        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    private void parsePacket(byte[] data, InetAddress address, int port) {
        String message = new String(data).trim();
        String type = message.substring(0, 2);
        switch (type) {
            default:
            case "00":
                Packet00Login loginPacket = new Packet00Login(data);
                Player player = new Player(game, loginPacket.username, address, port);
                addConnection(player, (Packet00Login) loginPacket);
                break;
            case "01":
                Packet01Move movePacket = new Packet01Move(data);
                handleMove(movePacket);
                break;
            case "02":
                Packet02Animation animPacket = new Packet02Animation(data);
                handleAnim(animPacket);
                break;
            case "03":
                Packet03Slash slashPacket = new Packet03Slash(data);
                handleSlash(slashPacket);
                break;

            case "04":
                Packet04Enemy enemyPacket = new Packet04Enemy(data);
                addEnemy(enemyPacket);
                handleEnemy(enemyPacket);
                break;
            case "05":
                Packet05Shot shotPacket = new Packet05Shot(data);
                shotPacket.writeData(this);
                Level1.handleShot(shotPacket.username);

                break;
        }
    }

    private void handleEnemy(Packet04Enemy enemyPacket) {
        for (Enemy enemy : connectedEnemies) {
            if (enemy.username.equals(enemyPacket.username)) {
                enemy.setX(enemyPacket.x);
                enemy.setY(enemyPacket.y);
                enemyPacket.writeData(this);
            }
        }
    }

    private void handleShot(Packet05Shot packet) {
        for (Player player : connectedPlayers) {
            if (player.username.equals(packet.username)) {
                if(player.isShot) {
                    player.isShot = false;
                    return;
                }
                player.lives -= 1;
                player.isShot = true;
            }
        }
    }

    private void handleSlash(Packet03Slash packet) {
        for (Player player : connectedPlayers) {
            if (player.username.equals(packet.username)) {
                player.isSlashing = true;
                player.destroyTile(packet.tileName);
                packet.writeData(this);
            }
        }
    }

    public void addEnemy(Enemy enemy) {
        connectedEnemies.add(enemy);
    }

    public void addEnemy(Packet04Enemy enemyPacket) {

        boolean alreadyConnected = false;
        for (Enemy e : connectedEnemies) {
            if (e.username.equalsIgnoreCase(enemyPacket.username)) {
                alreadyConnected = true;
            }
        }

        if (!alreadyConnected) {
            Enemy enemy = new Enemy(enemyPacket.username, Level1.grid, game, Level1.player);
            connectedEnemies.add(enemy);
        }
    }

    public void addConnection(Player player, Packet00Login packet) {
        boolean alreadyConnected = false;
        for (Player p : connectedPlayers) {
            if (player.username.equalsIgnoreCase(p.username)) {
                if (p.ipAddress == null) {
                    p.ipAddress = player.ipAddress;
                }
                if (p.port == -1) {
                    p.port = player.port;
                }
                alreadyConnected = true;
            } else {

                sendData(packet.getData(), p.ipAddress, p.port);
                packet = new Packet00Login(p.username, p.getX(), p.getY());
                sendData(packet.getData(), player.ipAddress, player.port);
            }
        }
        if (!alreadyConnected) {
            this.connectedPlayers.add(player);

        }
    }

    private void handleAnim (Packet02Animation packet) {
        for (Player player : connectedPlayers) {
            if (player.username.equals(packet.username)) {
                String animationName = packet.animationName;
                String mirrorLeft = packet.mirrorLeft;

                if (mirrorLeft.equals("true"))
                    player.mirrorLeft = true;
                else
                    player.mirrorLeft = false;

                if (animationName.equals("walk")) {
                    player.currentAnimation = player.walkAnimation;
                } else if (animationName.equals("jump")) {
                    player.currentAnimation = player.jumpAnimation;
                } else {
                    player.currentAnimation = player.idleAnimation;
                }

                packet.writeData(this);
            }
        }
    }
    private void handleMove(Packet01Move packet) {
        for (Player player : connectedPlayers) {
            if (player.username.equals(packet.username)) {
                player.setX(packet.x);
                player.setY(packet.y);
                packet.writeData(this);
            }
        }
    }

    public void sendData (byte[] data, InetAddress ipAddress, int port) {
        DatagramPacket packet = new DatagramPacket( data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDataToAllClients(byte[] data) {
        for (Player player : connectedPlayers) {
            sendData(data, player.ipAddress, player.port);
        }
    }


}
