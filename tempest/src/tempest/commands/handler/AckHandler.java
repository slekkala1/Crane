package tempest.commands.handler;

import tempest.commands.command.Ack;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;

import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * Created by swapnalekkala on 12/5/15.
 */
public class AckHandler implements ResponseCommandExecutor<Ack, String, String> {

    Set<Integer> ids;

    public boolean canHandle(Command.Message.Type type) {
        return Ack.type == type;
    }

    public AckHandler() {

    }

    public AckHandler(Set ids) {
        this.ids = ids;
    }

    public Command.Message serialize(Ack command) {
        Command.Ack.Builder ackBuilder = Command.Ack.newBuilder().setId(command.getId());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.ACK)
                .setAck(ackBuilder)
                .build();
        return message;
    }

    public Ack deserialize(Command.Message message) {
        Ack ack = new Ack();
        ack.setId(message.getAck().getId());
        return ack;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {
        this.ids.add(((Ack) command).getId());
        return "";
    }
}
