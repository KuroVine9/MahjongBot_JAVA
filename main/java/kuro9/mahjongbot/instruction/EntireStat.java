package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.data.UserGameData;
import kuro9.mahjongbot.exception.DBConnectException;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * 전체 범위의 유저 스탯을 출력합니다.
 */
public class EntireStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        long guildId = getGuildID(event);
        String gameGroup = getGameGroup(event);
        long userId = getValidUser(event).getIdLong();

        HashMap<Long, UserGameData> data_list;
        int[][] recent_game_list;

        try {
            data_list = DBScoreProcess.INSTANCE.getAllUserData(guildId, gameGroup, 0);
            recent_game_list = DBScoreProcess.INSTANCE.recentAllGameResult(guildId, userId, gameGroup);
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        UserGameData userGameData = data_list.get(userId);
        if (userGameData == null)
            userGameData = new UserGameData(userId);

        File scoreGraphImage = generateGraph(recent_game_list);

        event.getHook().sendMessageEmbeds(
                getEmbed(
                        userGameData,
                        String.format(resourceBundle.getString("stat.entire.embed.title"), getRank(data_list, userId), userGameData.getUserName()),
                        getValidUser(event).getEffectiveAvatarUrl(),
                        event.getUserLocale()
                ).build()
        ).addFiles(FileUpload.fromData(scoreGraphImage)).queue();
        Logger.addEvent(event);
    }
}
