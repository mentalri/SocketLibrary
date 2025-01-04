package de.mentalri.socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonHandler extends SocketHandler{
    public JsonHandler(SocketHandler handler) {
        super(handler);
    }
    public void sendJson(JsonElement element){
        try {
            sendData(element.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public JsonElement receiveJson(){
        try {
            return JsonParser.parseString(new String(readData(),StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
