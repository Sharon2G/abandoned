package wsuv.bounce;

public class Packet04Enemy {

    public String username;
    public float x, y;

    public Packet04Enemy(byte[] data) {
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.x = Float.parseFloat(dataArray[1]);
        this.y = Float.parseFloat(dataArray[2]);
    }

    public Packet04Enemy(String username, float x, float y) {
        this.username = username;
        this.x = x;
        this.y = y;
    }

    public void writeData(Client client) {
        client.sendData(getData());
    }

    public void writeData(Server server) {
        server.sendDataToAllClients(getData());
    }

    public byte[] getData() {
        return ("04" + this.username + "," + this.x + "," + this.y).getBytes();
    }

    public String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(2);
    }
}
