package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.json.simple.JSONArray;

import java.util.List;

import static Bots.Main.createQuickEmbed;

public class CommandDJ implements ICommand {
    public static JSONArray DJList = new JSONArray(); // i could not get this to work

    @Override
    public void execute(ExecuteArgs event) {
        //NOTE: Guild specific behaviour is gonna be needed for this at a later date -9382
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            List<Member> members = event.getMessage().getMentionedMembers(event.getGuild());
            DJList.add(members.get(0).getIdLong());
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You don't have the required permissions to do this.")).queue();
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String helpMessage() {
        return null;
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
