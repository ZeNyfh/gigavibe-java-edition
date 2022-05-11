package Bots;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

/*
This class is an extension of the MessageRecievedEvent that provides generally useful attributes for commands.

As of now its a bit dry, but this may get expanded at some point when something else could be seen as useful
-9382 */

public class MessageEvent extends MessageReceivedEvent {
    final List<String> args;

    public MessageEvent(MessageReceivedEvent event) {
        //Gain the previous MRE object properties.
        super(event.getJDA(), event.getResponseNumber(), event.getMessage()); //(Is this a good idea?)
        //New features on-top of MRE
        this.args = Arrays.asList(this.getMessage().getContentRaw().split(" "));
    }

    public List<String> getArgs() {
        return this.args;
    }
}
