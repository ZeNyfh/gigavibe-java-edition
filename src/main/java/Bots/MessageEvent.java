package Bots;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/*
This class is an extension of the MessageRecievedEvent that provides generally useful attributes for commands.

As of now its a bit dry, but this may get expanded at some point when something else could be seen as useful
-9382 */

public class MessageEvent {
    final String[] args;

    public MessageEvent(MessageReceivedEvent event) {
        //Could be used for more at some point
        this.args = event.getMessage().getContentRaw().split(" ");
    }

    public String[] getArgs() { return this.args; }
}
