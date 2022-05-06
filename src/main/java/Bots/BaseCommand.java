package Bots;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;

/* -9382
its 10AM and im too lazy to search for how to force an event into ExecuteArgs for ICommand.
So im just going to use my own base command here, since its gonna be much easier to work with.

NOTE: Consider using something that extends on top of MessageRecievedEvent, as pre-providing
elements like getArgs() could be quite useful
*/
public class BaseCommand {
    /* Would use an interface, however that prevents optional arguments, and I want getAlias() -9382 */
    public void execute(MessageReceivedEvent event) throws IOException {}

    public String getName() { return "default";}

    public String getCategory()  { return "default";}

    public String getDescription()  { return "default";}

    public ArrayList<String> getAlias() {
        ArrayList<String> aliases = new ArrayList<>();
        /* aliases.add("XYZ"); Example for aliases (Im aware its odd, but its just the system for now) -9382 */
        return aliases;
    }

}
