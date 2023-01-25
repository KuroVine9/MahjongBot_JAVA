package kuro9.mahjongbot;

import kuro9.mahjongbot.instruction.*;
import kuro9.mahjongbot.instruction.action.RankInterface;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
    private static RankInterface[] rank;
    private static StatInterface[] stat;

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
        ADMIN = jda.retrieveUserById(Setting.ADMIN_ID);
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.retrieveUserById(Setting.ADMIN_ID).map(User::getAsTag)
                .queue(name -> jda.getPresence().setActivity(Activity.competing("DM => " + name))
                );

        jda.addEventListener(new Main());

        System.out.println("[MahjongBot:Main] Initialize Complete!\n");

        System.out.println("[MahjongBot:Main] Loading Instructions...");
        CommandListUpdateAction commands = jda.updateCommands();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(Setting.INST_PATH));
            JSONArray jsonArray = (JSONArray) obj;
            jsonArray.stream().peek(
                    data -> System.out.printf("[MahjongBot:Main] Loaded Instruction \"%s\"\n", ((JSONObject) data).get("name"))
            ).forEach(data -> commands.addCommands(CommandData.fromData(DataObject.fromJson(data.toString()))));
        } catch (IOException | ParseException e) {
            System.out.println("\n\n[MahjongBot:Main] Runtime Instruction Loading Failure!\n\n");
            Logger.addSystemErrorEvent("instruction-load-err", ADMIN);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        commands.queue();
        rank = new RankInterface[]{new EntireRank(), new MonthRank(), new SeasonRank()};
        stat = new StatInterface[]{new EntireStat(), new MonthStat(), new SeasonStat()};
        System.out.println("[MahjongBot:Main] Instructions Loaded!");

        System.out.printf("[MahjongBot:Main] Bot Started! (%d ms)\n", System.currentTimeMillis() - time);
        Logger.addSystemEvent("system-start");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping" -> {
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true)
                        .flatMap(
                                v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                        ).queue();
            }
            case "add" -> Add.action(event, ADMIN);
            case "stat" -> stat[0].action(event);
            case "month_stat" -> stat[1].action(event);
            case "entire_stat" -> stat[2].action(event);
            case "revalid" -> ReValid.action(event, ADMIN);
            case "entire_rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[0].summaryReply(event);
                    case 1, -1 -> rank[0].umaReply(event);
                    case 2 -> rank[0].totalGameReply(event);
                }
            }
            case "month_rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[1].summaryReply(event);
                    case 1, -1 -> rank[1].umaReply(event);
                    case 2 -> rank[1].totalGameReply(event);
                }
            }
            case "rank" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[2].summaryReply(event);
                    case 1, -1 -> rank[2].umaReply(event);
                    case 2 -> rank[2].totalGameReply(event);
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().matches("^rank_uma.*")) rank[0].umaPageControl(event);
        else if (event.getComponentId().matches("^rank_totalgame.*")) rank[0].totalGamePageControl(event);
        else if (event.getComponentId().matches("^month_rank_uma.*")) rank[1].umaPageControl(event);
        else if (event.getComponentId().matches("^month_rank_totalgame.*")) rank[1].totalGamePageControl(event);
        else if (event.getComponentId().matches("^season_rank_uma.*")) rank[2].umaPageControl(event);
        else if (event.getComponentId().matches("^season_rank_totalgame.*")) rank[2].totalGamePageControl(event);
    }

}