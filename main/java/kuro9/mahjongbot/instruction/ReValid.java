package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.gdrive.GDrive;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;

/**
 * 순위 데이터 파일과 유저 데이터를 최신화합니다.
 */
public class ReValid {
    public static void action(SlashCommandInteractionEvent event, RestAction<User> ADMIN) {
        long time = System.currentTimeMillis();
        ScoreProcess.revalidData();
        GDrive.upload(Setting.FILE_ID, ADMIN);
        Logger.addEvent(event);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("PROCESSED");
        embed.setDescription(
                String.format("processing time : %d ms", 0)
        );
        event.replyEmbeds(embed.build()).setEphemeral(true).flatMap(
                v -> v.editOriginalEmbeds(
                        embed.setDescription(
                                String.format("processing time : %d ms", System.currentTimeMillis() - time)
                        ).build()
                )
        ).queue();
    }
}
