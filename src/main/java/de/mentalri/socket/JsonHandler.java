package de.mentalri.socket;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;

public class JsonHandler extends SocketHandler{
    public JsonHandler(SocketHandler handler) {
        super(handler);
    }
    public void sendJson(JsonNode node) throws Exception {
       sendData(node.toString().getBytes(StandardCharsets.UTF_8));
    }
}
