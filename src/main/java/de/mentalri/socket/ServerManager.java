package de.mentalri.socket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerManager {
    private static ServerManager instance;

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }
    protected final Logger logger = Logger.getLogger("ServerManager");
    protected Class<? extends Command> commandClass;
    protected boolean online = true;
    protected ServerManager() {
        // Initialize the server manager
    }

    public void run(int serverPort, Class<? extends Command> commandClass) {
        this.commandClass = commandClass;
        new Thread(() -> {
            logger.info("ServerManager started on port: " + serverPort);
            try {
                ServerSocket serverSocket = new ServerSocket(serverPort);
                while (online)
                {
                    handleConnection(serverSocket);
                }
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    public void handleConnection(ServerSocket serverSocket){
        try {
            Socket socket = serverSocket.accept();
            SocketHandler handler = new SocketHandler(socket);

            byte[] data = handler.readData();

            ObjectMapper objectMapper = new ObjectMapper();
            Command command = objectMapper.readValue(data, commandClass);
            execute(command, handler);

        }catch (IOException e) {
            logger.severe("Error while handling connection: " + e.getMessage());
        } catch (Exception e) {
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
