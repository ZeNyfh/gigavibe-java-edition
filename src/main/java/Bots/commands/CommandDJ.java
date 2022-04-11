package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

import static Bots.Main.createQuickEmbed;
import static Bots.commands.CommandBoosterDJ.DJList;

public class CommandDJ implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)){
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
