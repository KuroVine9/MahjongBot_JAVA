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
import java.util.Optional;
import java.util.ResourceBundle;

public class SeasonStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        HashMap<Long, UserGameData> data_list;

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);

        String gameGroup = getGameGroup(event);

        long userID = getValidUser(event).getIdLong();

        long guildID = getGuildID(event);

        try {
            data_list = DBScoreProcess.INSTANCE.getSelectedUserData(guildID, start_month, year, end_month, year, gameGroup, 0);
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }


        UserGameData user = Optional.ofNullable(data_list.get(userID)).orElseGet(() -> new UserGameData(userID));

        int rank = getRank(data_list, userID);

        File image = null;
        try {
            image = generateGraph(DBScoreProcess.INSTANCE.recentSelectedGameResult(guildID, userID, start_month, year, end_month, year, gameGroup));
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }
        event.getHook().sendMessageEmbeds(
                getEmbed(
                        user,
                        String.format(resourceBundle.getString("season_stat.embed.title"), rank, year, season, getValidUser(event).getEffectiveName()),
                        getValidUser(event).getEffectiveAvatarUrl(),
                        event.getUserLocale()
                ).build()
        ).addFiles(FileUpload.fromData(image)).queue();
        Logger.addEvent(event);
    }
}
