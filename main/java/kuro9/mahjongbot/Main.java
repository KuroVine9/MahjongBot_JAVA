package kuro9.mahjongbot;

import kuro9.mahjongbot.instruction.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    private static RestAction<User> ADMIN;

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        System.out.println("[MahjongBot:Main] System Initializing...");
        Setting.init();
        final String TOKEN;
        try {

            Scanner scan = new Scanner(new File(Setting.TOKEN_PATH));
            TOKEN = scan.next();
            scan.close();
        } catch (IOException e) {
            System.out.println("\n\n[MahjongBot:Main] Initialize Failure!\n\n");
            throw new RuntimeException(e);
        }
        JDA jda = JDABuilder.createDefault(TOKEN).build();
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.getPresence().setActivity(Activity.watching("?좊땲붾밾"));
        jda.addEventListener(new Main());
        ADMIN = jda.retrieveUserById(Setting.ADMIN_ID);
        System.out.println("[MahjongBot:Main] Initialize Complete!\n");

        System.out.println("[MahjongBot:Main] Loading Instructions...");
        CommandListUpdateAction commands = jda.updateCommands();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(Setting.INST_PATH));
            JSONArray jsonArray = (JSONArray) obj;
            jsonArray.stream().peek(
                    data -> System.out.printf("[MahjongBot:Main] Loaded Instruction \"%s\"\n", ((JSONObject) data).get("name").toString())
            ).forEach(data -> commands.addCommands(CommandData.fromData(DataObject.fromJson(data.toString()))));
        } catch (IOException | ParseException e) {
            System.out.println("\n\n[MahjongBot:Main] Runtime Instruction Loding Failure!\n\n");
            Logger.addSystemErrorEvent("instruction-load-err", ADMIN);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        commands.queue();
        System.out.println("[MahjongBot:Main] Instructions Loaded!");

        System.out.printf("[MahjongBot:Main] Bot Started! (%d ms)\n", System.currentTimeMillis() - time);
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        System.out.printf("[MahjongBot:Main] %s used /%s\n", event.getUser().getAsTag(), event.getName());
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
            case "add" -> Add.action(event, ADMIN);
            case "stat" -> SeasonStat.action(event);
            case "month_stat" -> MonthStat.action(event);
            case "entire_stat" -> EntireStat.action(event);
            case "revalid" -> ReValid.action(event);
            case "entire_rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> EntireRank.summaryReply(event);
                    case 1, -1 -> EntireRank.umaReply(event);
                    case 2 -> EntireRank.totalGameReply(event);
                }
            }
            case "month_rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> MonthRank.summaryReply(event);
                    case 1, -1 -> MonthRank.umaReply(event);
                    case 2 -> MonthRank.totalGameReply(event);
                }
            }
            case "rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> SeasonRank.summaryReply(event);
                    case 1, -1 -> SeasonRank.umaReply(event);
                    case 2 -> SeasonRank.totalGameReply(event);
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        System.out.printf("[MahjongBot:Main] %s used BUTTON.%s\n", event.getUser().getAsTag(), event.getComponentId());

        if (event.getComponentId().matches("^rank_uma.*")) EntireRank.umaPageControl(event);
        else if (event.getComponentId().matches("^rank_totalgame.*")) EntireRank.totalGamePageControl(event);
        else if (event.getComponentId().matches("^month_rank_uma.*")) MonthRank.umaPageControl(event);
        else if (event.getComponentId().matches("^month_rank_totalgame.*")) MonthRank.totalGamePageControl(event);
        else if (event.getComponentId().matches("^season_rank_uma.*")) SeasonRank.umaPageControl(event);
        else if (event.getComponentId().matches("^season_rank_totalgame.*")) SeasonRank.totalGamePageControl(event);
        else if (event.getComponentId().equals("buttonID")) event.editMessage("button!").queue();
    }

}