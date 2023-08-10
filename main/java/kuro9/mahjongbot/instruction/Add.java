package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.gdrive.GDrive;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 순위 데이터 파일에 기록을 추가합니다.
 */
public class Add {

    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        if (!event.isFromGuild()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("403 Forbidden");
            embed.addField(
                    resourceBundle.getString("add.embed.err.403.name"),
                    resourceBundle.getString("add.embed.err.403.description"),
                    true
            );
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).queue();

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG);
            return;
        }

        var options = event.getOptions();
        String[] names = new String[4];
        int[] scores = new int[4];
        for (int i = 0; i < options.size(); i++) {
            names[i / 2] = options.get(i).getAsUser().getName();
            scores[i / 2] = (int) options.get(++i).getAsLong();
        }

        int result = ScoreProcess.addScore(names, scores);
        switch (result) {
            case -1 -> {     // PARAM ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("400 Bad Request");
                embed.setDescription("Parameter err.");
                embed.addField(
                        resourceBundle.getString("add.embed.err.400.name"),
                        resourceBundle.getString("add.embed.err.400.description"),
                        true
                );
                embed.setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

                Logger.addErrorEvent(event, Logger.PARAM_ERR);
            }
            case -2 -> {    // IOEXCEPTION
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("404 Not Found");
                embed.setDescription("File I/O Exception");
                embed.addField(
                        resourceBundle.getString("add.embed.err.404.name"),
                        resourceBundle.getString("add.embed.err.404.description"),
                        true
                );
                embed.setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

                Logger.addErrorEvent(event, "file-not-found");
            }
            default -> {     // NO ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(resourceBundle.getString("add.embed.success.title"));
                for (int i = 0; i < 4; i++) {
                    embed.addField(
                            String.format(resourceBundle.getString("add.embed.success.field"), i + 1, names[i]),
                            String.valueOf(scores[i]),
                            true
                    );
                }
                embed.setFooter(
                        String.format(
                                resourceBundle.getString("add.embed.success.footer"),
                                result,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        )
                );
                embed.setColor(Color.BLACK);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
                Logger.addEvent(event);
                ScoreProcess.revalidData();
                GDrive.upload(Setting.DATA_FILE_ID, Setting.DATA_PATH);
            }
        }
    }
}
