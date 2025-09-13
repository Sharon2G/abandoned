package wsuv.bounce;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Packet02Animation {

    public String username;
    public String animationName;
    public String mirrorLeft;

    public Packet02Animation(byte[] data) {
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.animationName = dataArray[1];
        this.mirrorLeft = dataArray[2];
    }

    public Packet02Animation(String username, String animationName, String mirrorLeft) {
        this.username = username;
        this.animationName = animationName;
        this.mirrorLeft = mirrorLeft;
    }

    public void writeData(Client client) {
        client.sendData(getData());
    }

    public void writeData(Server server) {
        server.sendDataToAllClients(getData());
    }

    public byte[] getData() {
        return ("02" + this.username + "," + this.animationName  + "," + this.mirrorLeft).getBytes();
    }

    public String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(2);
    }
}
