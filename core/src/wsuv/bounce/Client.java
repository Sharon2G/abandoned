package wsuv.bounce;

import java.io.IOException;
import java.net.*;


public class Client extends Thread {
    private Game game;
    private InetAddress ipAddress;
    public static DatagramSocket socket;

    public Client (Game game, String ipAddress){
        this.game = game;
        try {
            socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
            socket.setSoTimeout(0);


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
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
            this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
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
                Level1.addPlayer(player);
                break;
            case "01":
                Packet01Move movePacket = new Packet01Move(data);
                Level1.movePlayer(movePacket.username, movePacket.x, movePacket.y);
                break;
            case "02":
                Packet02Animation animPacket = new Packet02Animation(data);
                Level1.handleAnim(animPacket.username, animPacket.animationName, animPacket.mirrorLeft);
                break;
            case "03":
                Packet03Slash slashPacket = new Packet03Slash(data);
                Level1.handleSlash(slashPacket.username, slashPacket.tileName);
                break;

            case "04":
                Packet04Enemy enemyPacket = new Packet04Enemy(data);
                if(Level1.enemies.size() < 2) {
                    Enemy enemy = new Enemy(enemyPacket.username, Level1.grid, game, Level1.player);
                    Level1.addEnemy(enemy);
                }
                Level1.moveEnemy(enemyPacket.username, enemyPacket.x, enemyPacket.y);
                break;

            case "05":
                Packet05Shot shotPacket = new Packet05Shot(data);
                Level1.handleShot(shotPacket.username);
                break;

        }
    }

    public void sendData (byte[] data) {
        DatagramPacket packet = new DatagramPacket( data, data.length, ipAddress, Level1.port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
