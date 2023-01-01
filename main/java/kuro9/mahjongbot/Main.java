package kuro9.mahjongbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Main extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        Scanner scan = null;
        final String TOKEN;
        try {
            scan = new Scanner(new File("C:\\token.txt"));
            TOKEN = scan.next();
        } catch (FileNotFoundException e) {
            System.out.println("TOKEN NOT FOUND");
            throw new RuntimeException(e);
        }

        scan.close();

        JDA jda = JDABuilder.createDefault(TOKEN).build();

        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.getPresence().setActivity(Activity.watching("?좊땲붾밾"));
        jda.addEventListener(new Main());

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                new CommandData("ping", "calc ping time of the bot")
        );
        commands.addCommands(
                new CommandData("name", "print name")
                        .addOptions(new OptionData(USER, "user", "user name to print", true))
        );

        commands.queue();

        System.out.println("Loaded!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("[DM] %s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());

        }
        else
            System.out.printf("[%s] [%s] %s: %s\n", event.getGuild().getName(), event.getChannel().getName()
            , event.getMember().getEffectiveName(), event.getMessage().getContentDisplay());
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event){
        switch(event.getName()){
            case "ping" -> {
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true)
                        .flatMap(
                                v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                        ).queue();
            }
            case "name" -> {
                event.reply(String.format("UserName: %s", event.getOption("user").getAsUser().getAsTag())).queue();
            }
            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }
}