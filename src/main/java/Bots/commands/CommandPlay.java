package Bots.commands;

import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.URI;
import java.net.URISyntaxException;

import static Bots.Main.createQuickEmbed;

public class CommandPlay implements ICommand {

    @Override
    public void execute(ExecuteArgs event) {

        if(!event.getMemberVoiceState().inAudioChannel()){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc.")).queue();
            return;
        }

        if(!event.getSelfVoiceState().inAudioChannel()){
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMemberVoiceState().getChannel();
            audioManager.openAudioConnection(memberChannel);
        } else if (event.getMemberVoiceState().getChannel() != event.getSelfVoiceState().getChannel()){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in the same vc.")).queue();
            return;
        }

        String link = String.join(" ", event.getArgs());

        if(!isUrl(link)){
            link = "ytsearch:" + link;
            link = link.replace("&play", "");
            if (link.contains("youtu.be/")){
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        }

        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link);

    }
    private boolean isUrl(String url){
        try{
            new URI(url);
            return true;
        } catch (URISyntaxException e){
            return false;
        }
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String helpMessage() {
        return "Plays songs or playlists from youtube.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}