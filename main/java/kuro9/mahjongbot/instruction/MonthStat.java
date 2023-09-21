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

public class MonthStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int month = getValidMonth(event);
        int year = getValidYear(event);
        long guildId = getGuildID(event);
        String gameGroup = getGameGroup(event);
        long userId = getValidUser(event).getIdLong();

        HashMap<Long, UserGameData> data_list;
        int[][] recent_game_list;

        try {
            data_list = DBScoreProcess.INSTANCE.getMonthUserData(guildId, month, year, gameGroup, 0);
            recent_game_list = DBScoreProcess.INSTANCE.recentMonthGameResult(guildId, userId, month, year, gameGroup);
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
                        String.format(resourceBundle.getString("stat.month.embed.title"), getRank(data_list, userId), year, month, userGameData.getUserName()),
                        getValidUser(event).getEffectiveAvatarUrl(),
                        event.getUserLocale()
                ).build()
        ).addFiles(FileUpload.fromData(scoreGraphImage)).queue();
        Logger.addEvent(event);
    }
}
