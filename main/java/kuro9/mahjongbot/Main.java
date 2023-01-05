package kuro9.mahjongbot;

import net.dv8tion.jda.api.EmbedBuilder;
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
import java.awt.*;
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
                        ),
                new CommandData("stat", "stat")
                        .addOptions(
                                new OptionData(USER, "user", "user")
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
            case "add" -> add(event);

            case "stat" -> stat(event);
            default -> throw new IllegalStateException("Unexpected value: " + event.getName());
        }
    }

    public void add(SlashCommandEvent event) {
        if (!event.isFromGuild()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("403 Forbidden");
            embed.addField(
                    "정상적인 경로에서 명령어를 실행하여 주십시오.",
                    "에러 이벤트는 디버깅을 위해 저장됩니다.",
                    true
            );
            embed.setColor(Color.RED);
            event.replyEmbeds(embed.build()).queue();

            ErrorLogger logger = new ErrorLogger(Setting.LOG_PATH);
            logger.addErrorEvent(event, "not-guild-msg", ADMIN);
            return;
        }

        var options = event.getOptions();
        String[] names = new String[4];
        int[] scores = new int[4];
        for (int i = 0; i < options.size(); i++) {
            names[i / 2] = options.get(i).getAsUser().getName().replaceAll(" ", "");
            scores[i / 2] = (int) options.get(++i).getAsLong();
        }

        var process = new ScoreProcess();
        int result = process.addScore(names, scores);
        switch (result) {
            case -1 -> {     // PARAM ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("400 Bad Request");
                embed.setDescription("Parameter err.");
                embed.addField(
                        "다음의 항목을 확인해 주십시오.",
                        "``` - 점수의 총합\n - 점수의 정렬 여부\n - 중복된 이름```",
                        true
                );
                embed.setColor(Color.RED);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();

                ErrorLogger logger = new ErrorLogger(Setting.LOG_PATH);
                logger.addErrorEvent(event, "parameter-err", ADMIN);
            }
            case -2 -> {    // IOEXCEPTION
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("404 Not Found");
                embed.setDescription("File I/O Exception");
                embed.addField(
                        "순위 파일을 찾을 수 없습니다.",
                        "잠시 후 다시 시도해 주세요.",
                        true
                );
                embed.setColor(Color.RED);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();

                ErrorLogger logger = new ErrorLogger(Setting.LOG_PATH);
                logger.addErrorEvent(event, "file-not-found", ADMIN);
            }
            default -> {     // NO ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("패보 기록완료");
                for (int i = 0; i < 4; i++) {
                    embed.addField(
                            String.format("%d위 : %s", i + 1, names[i]),
                            String.valueOf(scores[i]),
                            true
                    );
                }
                embed.setFooter(String.valueOf(result));
                embed.setColor(Color.BLACK);
                event.replyEmbeds(embed.build()).queue();
            }
        }
    }

    public void stat(SlashCommandEvent event) {
        String select;
        if (event.getOption("user") == null) select = event.getUser().getName();
        else select = event.getOption("user").getAsUser().getName();
        select = select.replaceAll(" ", "");

        ScoreProcess sp = new ScoreProcess();
        UserGameData userdata = null;
        for (var user : sp.getUserDataList()) if (user.name.equals(select)) userdata = user;
        // TODO
    }
}