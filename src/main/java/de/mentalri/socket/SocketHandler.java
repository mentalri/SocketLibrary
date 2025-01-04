package de.mentalri.socket;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketHandler {
    protected final Socket socket;
    protected final SocketHandler handler;
    public SocketHandler(Socket socket) {
        this.socket = socket;
        this.handler = null;
    }

    public SocketHandler(SocketHandler handler) {
        this.socket = handler.socket;
        this.handler = handler;
    }

    public void sendData(byte[] dataBytes) throws IOException {
        int dataLength = dataBytes.length;
        byte[] header = ByteBuffer.allocate(4).putInt(dataLength).array();
        socket.getOutputStream().write(header);
        socket.getOutputStream().write(dataBytes);
    }

    public byte[] readData() throws IOException {
        byte[] headerBuffer= new byte[4];
        socket.getInputStream().read(headerBuffer);
        int size = ByteBuffer.wrap(headerBuffer).getInt();
        byte[] dataBuffer = new byte[size];
        socket.getInputStream().read(dataBuffer);
        return dataBuffer;
    }


}
