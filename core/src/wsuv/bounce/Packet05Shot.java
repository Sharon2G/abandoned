package wsuv.bounce;

public class Packet05Shot {

    public String username;

    public Packet05Shot(byte[] data) {
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
    }

    public Packet05Shot(String username) {
        this.username = username;
    }

    public void writeData(Client client) {
        client.sendData(getData());
    }

    public void writeData(Server server) {
        server.sendDataToAllClients(getData());
    }

    public byte[] getData() {
        return ("05" + this.username).getBytes();
    }

    public String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(2);
    }
}
