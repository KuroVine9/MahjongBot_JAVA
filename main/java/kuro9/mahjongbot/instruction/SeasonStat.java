package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.exception.DBConnectException;
import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.data.UserGameData;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public class SeasonStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        HashMap<Long, UserGameData> data_list;

        int season = ((event.getOption("season") == null) ?
                ((LocalDateTime.now().getMonthValue() - 1) / 6) + 1 :
                (int) event.getOption("season").getAsLong());
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = ((event.getOption("year") == null) ?
                LocalDate.now().getYear() :
                (int) event.getOption("year").getAsLong());

        String gameGroup = ((event.getOption("game_group") == null) ?
                "" : event.getOption("game_group").getAsString());

        long userID = getValidUser(event).getIdLong();

        long guildID;
        if (event.getOption("guild") == null) {
            if (event.getGuild() == null) {
                throw new RuntimeException("Unexpected Condition!! - guildID parse");
            }
            else guildID = event.getGuild().getIdLong();
        }
        else guildID = event.getOption("guild").getAsLong();

        try {
            data_list = DBScoreProcess.INSTANCE.getSelectedUserData(guildID, start_month, year, end_month, year, gameGroup, 0);
        }
        catch (DBConnectException e) {
            e.printStackTrace();
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()).build()).queue();
            return;
        }


        UserGameData user = Optional.ofNullable(data_list.get(userID)).orElseGet(() -> new UserGameData(userID));

        int rank = getRank(data_list, userID);

        File image = generateGraph(DBScoreProcess.INSTANCE.recentSelectedGameResult(guildID, userID, start_month, year, end_month, year, gameGroup));
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
