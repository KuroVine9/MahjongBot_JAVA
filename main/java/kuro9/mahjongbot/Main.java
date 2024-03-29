package kuro9.mahjongbot;

import kuro9.mahjongbot.instruction.*;
import kuro9.mahjongbot.instruction.action.RankInterface;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
    private static RankInterface[] rank;
    private static StatInterface[] stat;

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        System.out.println("[MahjongBot:Main] System Initializing...");
        Setting.parseString();

        final String TOKEN;
        try {
            Scanner scan = new Scanner(new File(Setting.TOKEN_PATH));
            TOKEN = scan.next();
            scan.close();
        }
        catch (IOException e) {
            System.out.println("\n\n[MahjongBot:Main] Initialize Failure!\n\n");
            throw new RuntimeException(e);
        }
        Setting.init(TOKEN);

        JDA jda = Setting.JDA;
        if (jda == null) {
            System.out.println("\n\n[MahjongBot:Main] JDA Cannot be Null!\n\n");
            throw new NullPointerException(JDA.class.getName());
        }

        Setting.setAdmin(jda.retrieveUserById(Setting.ADMIN_ID));
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.retrieveUserById(Setting.ADMIN_ID).map(User::getName)
                .queue(name -> jda.getPresence().setActivity(Activity.competing("DM => " + name)));

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
        }
        catch (IOException | ParseException e) {
            System.out.println("\n\n[MahjongBot:Main] Runtime Instruction Loading Failure!\n\n");
            Logger.addSystemErrorEvent(Logger.INSTRUCTION_LOAD_ERR);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        commands.queue();
        rank = new RankInterface[]{new EntireRank(), new MonthRank(), new SeasonRank()};
        stat = new StatInterface[]{new EntireStat(), new MonthStat(), new SeasonStat()};
        System.out.println("[MahjongBot:Main] Instructions Loaded!");

        System.out.printf("[MahjongBot:Main] Bot Started! (%d ms)\n", System.currentTimeMillis() - time);
        Logger.addSystemEvent(Logger.SYS_START);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getFullCommandName()) {
            case "ping" -> {
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true)
                        .flatMap(
                                v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                        ).queue();
                Logger.addEvent(event);
            }
            case "revalid" -> ReValid.action(event);
            case "file" -> {
                Logger.addEvent(event);
                event.reply("Uploaded Files\n> `sunwi.csv` will not be use or update.").setEphemeral(true).addActionRow(
                        Button.link(String.format("https://docs.google.com/spreadsheets/d/%s/", Setting.DATA_FILE_ID), "sunwi.csv"),
                        Button.link(String.format("https://docs.google.com/spreadsheets/d/%s/", Setting.LOG_FILE_ID), "log.csv"),
                        Button.link(String.format("https://docs.google.com/spreadsheets/d/%s/", Setting.ERROR_LOG_FILE_ID), "error_log.csv")
                ).queue();
            }
            case "add" -> AddScore.action(event);

            case "stat season" -> stat[2].action(event);
            case "stat month" -> stat[1].action(event);
            case "stat entire" -> stat[0].action(event);


            case "rank entire" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[0].summaryReply(event);
                    case 1, -1 -> rank[0].umaReply(event);
                    case 2 -> rank[0].totalGameReply(event);
                }
            }
            case "rank month" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[1].summaryReply(event);
                    case 1, -1 -> rank[1].umaReply(event);
                    case 2 -> rank[1].totalGameReply(event);
                }
            }
            case "rank season" -> {
                switch (event.getOption("type") == null ? -1 : (int) event.getOption("type").getAsLong()) {
                    case 0 -> rank[2].summaryReply(event);
                    case 1, -1 -> rank[2].umaReply(event);
                    case 2 -> rank[2].totalGameReply(event);
                }
            }
            case "machi" -> MahjongCalc.getAllMachi(event);

            case "admin add" -> AddAdmin.INSTANCE.action(event);
            case "admin get" -> GetAdminList.INSTANCE.action(event);
            case "admin delete" -> DeleteAdmin.INSTANCE.action(event);

            case "game_group add" -> AddGameGroup.INSTANCE.action(event);
            case "game_group get" -> GetGameGroupList.INSTANCE.action(event);

            case "delete" -> DeleteScore.INSTANCE.action(event);
            case "modify" -> ModifyScore.INSTANCE.action(event);

            default -> {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("501 Not Implemented");
                embed.setDescription("Unexpected value: " + event.getName());
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                Logger.addErrorEvent(event, Logger.UNKNOWN_INST);
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (id.matches("^rank_uma.*")) rank[0].umaPageControl(event);
        else if (id.matches("^rank_totalgame.*")) rank[0].totalGamePageControl(event);
        else if (id.matches("^month_rank_uma.*")) rank[1].umaPageControl(event);
        else if (id.matches("^month_rank_totalgame.*")) rank[1].totalGamePageControl(event);
        else if (id.matches("^season_rank_uma.*")) rank[2].umaPageControl(event);
        else if (id.matches("^season_rank_totalgame.*")) rank[2].totalGamePageControl(event);
        else if (id.matches("^delete.*")) DeleteScore.INSTANCE.confirm(event);
        else if (id.matches("^modify.*")) ModifyScore.INSTANCE.confirm(event);
    }

}