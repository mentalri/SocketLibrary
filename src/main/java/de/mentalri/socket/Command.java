package de.mentalri.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;

@Setter
public abstract class Command {
    private ObjectNode sender;
    public abstract void execute(SocketHandler handler) throws Exception;
    public ObjectNode jsonObject(){
        return new ObjectMapper().createObjectNode();
    }
    public ArrayNode jsonArray() {
        return new ObjectMapper().createArrayNode();
    }
}
