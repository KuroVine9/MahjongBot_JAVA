package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static kuro9.mahjongbot.Logger.PERMISSION_DENY;

/**
 * 캐시를 무효화합니다.
 */
public class ReValid {
    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        if (event.getUser().getIdLong() != Setting.ADMIN_ID) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("PERMISSION DENIED");
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            Logger.addErrorEvent(event, PERMISSION_DENY);
            return;
        }

        long time = System.currentTimeMillis();
        DBScoreProcess.INSTANCE.deleteAllCacheData();
        Logger.addEvent(event);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("PROCESSED");
        embed.setDescription(
                String.format("processing time : %d ms", System.currentTimeMillis() - time)
        );
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
