package Bots;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONObject;

/**
 * An extension of the MessageReceivedEvent that provides generally useful attributes for commands.
 */
public class MessageEvent extends MessageReceivedEvent {
    final String[] args;
    final JSONObject config;

    public MessageEvent(MessageReceivedEvent event) {
        //Gain the previous MRE object properties.
        super(event.getJDA(), event.getResponseNumber(), event.getMessage()); //(Is this a good idea?)
        //New features on-top of MRE
        this.args = this.getMessage().getContentRaw().split(" ");
        this.config = ConfigManager.GetConfig(this.getGuild().getIdLong());
    }

    public String[] getArgs() {
        return this.args;
    }

    public JSONObject getConfig() {
        return this.config;
    }
}
