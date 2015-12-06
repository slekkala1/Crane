package tempest.commands.handler;

import tempest.commands.command.Spout;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;
import tempest.services.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class SpoutHandler implements ResponseCommandExecutor<Spout, String, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == Spout.type;
    }

    public Command.Message serialize(Spout command) {
        Command.Spout.Builder spoutBuilder = Command.Spout.newBuilder();
        if (command.getResponse() != null)
            spoutBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.SPOUT)
                .setSpout(spoutBuilder)
                .build();
        return message;
    }

    public Spout deserialize(Command.Message message) {
        Spout spout = new Spout();
        if (message.hasSpout() && message.getSpout().hasResponse())
            spout.setResponse(message.getSpout().getResponse());
        return spout;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {

        return "ok";
    }
}
