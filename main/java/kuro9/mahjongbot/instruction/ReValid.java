package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ReValid {
    public ReValid(SlashCommandEvent event) {
        long time = System.currentTimeMillis();
        new ScoreProcess().revalidData();
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
