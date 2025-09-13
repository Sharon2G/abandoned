package wsuv.bounce;

public class Packet03Slash {

    public String username;
    public String tileName;

    public Packet03Slash(byte[] data) {
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.tileName = dataArray[1];
    }

    public Packet03Slash(String username, String tileName) {
        this.username = username;
        this.tileName = tileName;
    }

    public void writeData(Client client) {
        client.sendData(getData());
    }

    public void writeData(Server server) {
        server.sendDataToAllClients(getData());
    }

    public byte[] getData() {
        return ("03" + this.username + "," + this.tileName).getBytes();
    }

    public String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(2);
    }
}
