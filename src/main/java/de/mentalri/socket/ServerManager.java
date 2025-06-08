package de.mentalri.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ServerManager {
    protected final Logger logger = Logger.getLogger("ServerManager");
    @Getter
    protected final ArrayList<byte[]> receivedData = new ArrayList<>();
    protected Class<? extends Command> commandClass;
    protected boolean online = true;
    protected Thread serverThread;
    protected ServerSocket serverSocket;

    protected ServerManager() {
        // Initialize the server manager
    }
    public void stop() {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.severe("Error while closing server socket: " + e.getMessage());
            }
        }
    }

    public void run(int serverPort, Class<? extends Command> commandClass) {
        stop();
        this.commandClass = commandClass;
        serverThread = new Thread(() -> {
            logger.info("ServerManager started on port: " + serverPort);
            try {
                serverSocket = new ServerSocket(serverPort);
                while (online && Thread.currentThread().isAlive() && !Thread.currentThread().isInterrupted())
                {
                    handleConnection();
                }
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
    }
    public void handleConnection(){
        try {
            SocketHandler handler = new SocketHandler(serverSocket.accept());

            byte[] data = handler.readData();
            receivedData.add(data);
            ObjectMapper objectMapper = new ObjectMapper();
            Command command = objectMapper.readValue(data, commandClass);
            execute(command, handler);

        }catch (Exception e) {
            logger.severe("Error while handling connection: " + e.getMessage());
        }
    }
    public void execute(Command command, SocketHandler handler) throws Exception {
        try {
            command.execute(handler);
        }
        catch (Exception e) {
            handler.sendData(
                    new ObjectMapper().createObjectNode()
                            .put("result", "error")
                            .toString()
                            .getBytes()
            );
            throw e;
        }finally {
            handler.close();
        }
    }
}
