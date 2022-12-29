package Bots;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An extension of the MessageReceivedEvent that provides generally useful attributes for commands.
 * Can take either a SlashCommandInteractionEvent or MessageReceivedEvent as the input
 *
 * @author 9382
 * @version 2.1.1
 */
public class MessageEvent {
    final Object coreEvent;
    final JDA JDA;
    final Guild guild;
    final MessageChannelUnion channel;
    final Member member;
    final User user;
    final String[] args;
    final OptionMapping[] options;
    final String rawContent;
    final List<Message.Attachment> attachments;
    final JSONObject config;

    public boolean isSlash() {
        return this.coreEvent.getClass() == SlashCommandInteractionEvent.class;
    }

    public MessageEvent(MessageReceivedEvent event) {
        this.coreEvent = event;
        this.JDA = event.getJDA();
        this.guild = event.getGuild();
        this.channel = event.getChannel();
        this.member = event.getMember();
        this.user = event.getAuthor();
        this.args = event.getMessage().getContentRaw().split(" ");
        this.options = new OptionMapping[0]; //Not a thing outside of slash commands
        this.rawContent = event.getMessage().getContentRaw();
        this.attachments = event.getMessage().getAttachments();
        this.config = ConfigManager.GetGuildConfig(event.getGuild().getIdLong());
    }

    public MessageEvent(SlashCommandInteractionEvent event) {
        this.coreEvent = event;
        this.JDA = event.getJDA();
        this.guild = event.getGuild();
        this.channel = event.getChannel();
        this.member = event.getMember();
        this.user = Objects.requireNonNull(event.getMember()).getUser();

        List<OptionMapping> options = event.getInteraction().getOptions();
        List<String> args = new ArrayList<>();
        args.add("/" + event.getCommandPath());
        List<Message.Attachment> Attachments = new ArrayList<>();
        for (OptionMapping option : options) {
            if (option.getType() == OptionType.ATTACHMENT) {
                Attachments.add(option.getAsAttachment());
            } else {
                args.add(option.getAsString());
            }
        }
        this.args = args.toArray(new String[0]);
        this.options = options.toArray(new OptionMapping[0]);
        this.attachments = Attachments;
        this.rawContent = String.join(" ", args);

        if (event.getGuild() != null) {
            this.config = ConfigManager.GetGuildConfig(event.getGuild().getIdLong());
        } else {
            this.config = new JSONObject();
        }
    }

    public JDA getJDA() {
        return this.JDA;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public MessageChannelUnion getChannel() {
        return this.channel;
    }

    public Member getMember() {
        return this.member;
    }

    public User getUser() {
        return this.user;
    }

    public String[] getArgs() {
        return this.args;
    }

    public OptionMapping[] getOptions() {
        return this.options;
    }

    public List<Message.Attachment> getAttachments() {
        return this.attachments;
    }

    public String getContentRaw() {
        return this.rawContent;
    }

    public boolean isAcknowledged() {
        return isSlash() && ((SlashCommandInteractionEvent) this.coreEvent).isAcknowledged();
    }

    public static class Response {
        //Bad type conversion practices, the sequel
        final Object coreObject;

        public boolean isSlash() {
            return this.coreObject.getClass() == InteractionHookImpl.class;
        }

        public Response(InteractionHook interaction) {
            this.coreObject = interaction;
        }

        public Response(Message message) {
            this.coreObject = message;
        }

        public void delete() {
            if (isSlash()) {
                ((InteractionHookImpl) this.coreObject).deleteOriginal().queue();
            } else {
                ((Message) this.coreObject).delete().queue();
            }
        }

        public void editMessage(String s) {
            if (isSlash()) {
                ((InteractionHookImpl) this.coreObject).editOriginal(s).queue();
            } else {
                ((Message) this.coreObject).editMessage(s).queue();
            }
        }

        public void editMessageFormat(String s, Object... objects) {
            if (isSlash()) {
                ((InteractionHookImpl) this.coreObject).editOriginalFormat(s, objects).queue();
            } else {
                ((Message) this.coreObject).editMessageFormat(s, objects).queue();
            }
        }

        public void editMessageEmbeds(MessageEmbed... embeds) {
            if (isSlash()) {
                ((InteractionHookImpl) this.coreObject).editOriginalEmbeds(embeds).queue();
            } else {
                ((Message) this.coreObject).editMessageEmbeds(embeds).queue();
            }
        }

        public void editMessageFiles(FileUpload... files) {
            if (isSlash()) {
                ((InteractionHookImpl) this.coreObject).editOriginalAttachments(files).queue();
            } else {
                ((Message) this.coreObject).editMessageAttachments(files).queue();
            }
        }
    }

    public void reply(String s) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).reply(s).queue();
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().reply(s).queue();
        }
    }

    public void reply(Consumer<MessageEvent.Response> lambda, String s) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).reply(s).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().reply(s).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        }
    }

    public void replyEmbeds(MessageEmbed embed, MessageEmbed... embeds) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).replyEmbeds(embed, embeds).queue();
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().replyEmbeds(embed, embeds).queue();
        }
    }

    public void replyEmbeds(Consumer<MessageEvent.Response> lambda, MessageEmbed embed, MessageEmbed... embeds) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).replyEmbeds(embed, embeds).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().replyEmbeds(embed, embeds).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        }
    }

    public void replyFiles(FileUpload... files) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).replyFiles(files).queue();
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().replyFiles(files).queue();
        }
    }

    public void replyFiles(Consumer<MessageEvent.Response> lambda, FileUpload... files) {
        if (isSlash()) {
            ((SlashCommandInteractionEvent) this.coreEvent).replyFiles(files).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        } else {
            ((MessageReceivedEvent) this.coreEvent).getMessage().replyFiles(files).queue(x -> lambda.accept(new MessageEvent.Response(x)));
        }
    }

    public JSONObject getConfig() {
        return this.config;
    }
}
