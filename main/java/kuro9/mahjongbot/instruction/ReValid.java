package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * 순위 데이터 파일과 유저 데이터를 최신화합니다.
 */
public class ReValid {
    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        long time = System.currentTimeMillis();
        DBScoreProcess.DataCache.INSTANCE.invalidAllData();
        Logger.addEvent(event);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("PROCESSED");
        embed.setDescription(
                String.format("processing time : %d ms", System.currentTimeMillis() - time)
        );
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
