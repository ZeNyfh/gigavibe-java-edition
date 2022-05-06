package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;

import java.util.List;

import static Bots.Main.createQuickEmbed;

public class CommandDJ extends BaseCommand {
    public static JSONArray DJList = new JSONArray(); // i could not get this to work

    public void execute(MessageReceivedEvent event) {
        //NOTE: Guild specific behaviour is gonna be needed for this at a later date -9382
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            List<Member> members = event.getMessage().getMentionedMembers(event.getGuild());
            DJList.add(members.get(0).getIdLong());
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You don't have the required permissions to do this.")).queue();
        }
    }

    public String getCategory() {
        return "Admin";
    }

    public String getName() {
        return "dj";
    }

    public String getDescription() {
        return "<[Role]>` - Sets a role to have dj permissions or shows all current DJs.";
    }
}
