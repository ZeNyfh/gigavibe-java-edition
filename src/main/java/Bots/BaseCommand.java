package Bots;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;

/* -9382
its 10AM and im too lazy to search for how to force an event into ExecuteArgs for ICommand.
So im just going to use my own base command here, since its gonna be much easier to work with.

NOTE: Consider using something that extends on top of MessageRecievedEvent, as pre-providing
elements like getArgs() could be quite useful
*/
public interface BaseCommand {
    void execute(MessageReceivedEvent event) throws IOException;

    String getName();

    String getCategory();

    String getDescription();

    /* ArrayList<String> getAlias(); Optional */
}
