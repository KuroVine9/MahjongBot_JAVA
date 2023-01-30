package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.gdrive.GDrive;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * 순위 데이터 파일과 유저 데이터를 최신화합니다.
 */
public class ReValid {
    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        long time = System.currentTimeMillis();
        ScoreProcess.revalidData();
        GDrive.upload(Setting.DATA_FILE_ID, Setting.DATA_PATH);
        GDrive.upload(Setting.LOG_FILE_ID, Setting.LOG_PATH);
        GDrive.upload(Setting.ERROR_LOG_FILE_ID, Setting.ERROR_LOG_PATH);
        Logger.addEvent(event);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("PROCESSED");
        embed.setDescription(
                String.format("processing time : %d ms", System.currentTimeMillis() - time)
        );
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
