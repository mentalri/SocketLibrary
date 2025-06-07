package de.mentalri.socket;

public abstract class Command {
    public abstract void execute(SocketHandler handler) throws Exception;

}
