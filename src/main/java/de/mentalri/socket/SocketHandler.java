package de.mentalri.socket;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketHandler implements AutoCloseable{
    @Getter
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
        int dataRead = 0;
        long startTime = System.currentTimeMillis();
        while (dataRead < size) {
            if (System.currentTimeMillis() - startTime > 1000) {
                throw new IOException("Timeout while reading data");
            }
            int bytesRead = socket.getInputStream().read(dataBuffer, dataRead, size - dataRead);
            if (bytesRead == -1) {
                throw new IOException("Stream closed before reading all data");
            }
            dataRead += bytesRead;
        }
        return dataBuffer;
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public void close() throws IOException {
        socket.close();
    }

}
