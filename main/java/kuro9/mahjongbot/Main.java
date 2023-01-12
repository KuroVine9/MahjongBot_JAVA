package kuro9.mahjongbot;

import kuro9.mahjongbot.instruction.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    private static RestAction<User> ADMIN;

    public static void main(String[] args) throws LoginException {
        System.out.println("System Initializing...");
        Setting.init();
        final String TOKEN;
        try {

            Scanner scan = new Scanner(new File(Setting.TOKEN_PATH));
            TOKEN = scan.next();
            scan.close();
        } catch (IOException e) {
            System.out.println("\n\nInitialize Failure!\n\n");
            throw new RuntimeException(e);
        }
        JDA jda = JDABuilder.createDefault(TOKEN).build();
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.getPresence().setActivity(Activity.watching("?좊땲붾밾"));
        jda.addEventListener(new Main());
        ADMIN = jda.retrieveUserById(Setting.ADMIN_ID);
        System.out.println("Initialize Complete!\n");

        System.out.println("Loading Instructions...");
        CommandListUpdateAction commands = jda.updateCommands();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(Setting.INST_PATH));
            JSONArray jsonArray = (JSONArray) obj;
            jsonArray.stream().peek(
                    data -> System.out.printf("Loaded Instruction [%s]\n", data.toString().split(",|:", 5)[3])
            ).forEach(data -> commands.addCommands(CommandData.fromData(DataObject.fromJson(data.toString()))));
        } catch (IOException | ParseException e) {
            System.out.println("\n\nRuntime Instruction Loding Failure!\n\n");
            Logger.addSystemErrorEvent("instruction-load-err", ADMIN);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        commands.queue();
        System.out.println("Instructions Loaded!");

        System.out.println("Started!");
        Logger.addSystemEvent("system-start");
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
                event.reply(
                        String.format("UserName: %s", event.getOption("user").getAsUser().getAsTag())
                ).addActionRow(
                        Button.primary("buttonID", "buttonName")
                ).queue();
            }
            case "add" -> new Add(event, ADMIN);
            case "stat" -> new Stat(event);
            case "month_stat" -> new MonthStat(event);
            case "revalid" -> new ReValid(event);
            case "rank" -> {
                Rank r = new Rank();
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> r.summaryReply(event);
                    case 1, -1 -> r.umaReply(event);
                    case 2 -> r.totalGameReply(event);
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if (event.getComponentId().matches("^rank_uma.*")) new Rank().umaPageControl(event);
        else if (event.getComponentId().matches("^rank_totalgame.*")) new Rank().totalGamePageControl(event);
        else if (event.getComponentId().equals("buttonID")) event.editMessage("button!").queue();
    }

}