package kuro9.mahjongbot;

import kuro9.mahjongbot.instruction.Add;
import kuro9.mahjongbot.instruction.MonthStat;
import kuro9.mahjongbot.instruction.Stat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class Main extends ListenerAdapter {
    private static RestAction<User> ADMIN;

    public static void main(String[] args) throws LoginException {
        Setting.init();
        Scanner scan = null;
        final String TOKEN;
        try {
            scan = new Scanner(new File(Setting.TOKEN_PATH));
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
        commands.addCommands(
                new CommandData("msg", "msgtest")
        );
        commands.addCommands(
                new CommandData("add", "add")
                        .addOptions(
                                new OptionData(USER, "1st_name", "test", true),
                                new OptionData(INTEGER, "1st_score", "test", true),
                                new OptionData(USER, "2nd_name", "test", true),
                                new OptionData(INTEGER, "2nd_score", "test", true),
                                new OptionData(USER, "3rd_name", "test", true),
                                new OptionData(INTEGER, "3rd_score", "test", true),
                                new OptionData(USER, "4th_name", "test", true),
                                new OptionData(INTEGER, "4th_score", "test", true)
                        )
        );
        commands.addCommands(
                new CommandData("stat", "stat")
                        .addOptions(
                                new OptionData(USER, "user", "user")
                        )
        );
        commands.addCommands(
                new CommandData("month_stat", "month_stat")
                        .addOptions(
                                new OptionData(USER, "user", "user"),
                                new OptionData(INTEGER, "month", "month"),
                                new OptionData(INTEGER, "year", "year")
                        )
        );
        commands.queue();

        ADMIN = jda.retrieveUserById(Setting.ADMIN_ID);

        System.out.println("Loaded!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("[DM] %s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());

        }
        else
            System.out.printf("[%s] [%s] %s: %s\n", event.getGuild().getName(), event.getChannel().getName()
                    , event.getMember().getEffectiveName(), event.getMessage().getContentDisplay());
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        switch (event.getName()) {
            case "msg" -> {
                ADMIN.queue(
                        admin -> admin.openPrivateChannel().queue(
                                privateChannel -> privateChannel.sendMessage("test").queue()
                        )
                );
            }
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
            case "add" -> new Add(event, ADMIN);
            case "stat" -> new Stat(event);
            case "month_stat" -> new MonthStat(event);

            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

}