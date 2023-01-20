package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class SeasonStat extends StatArranger {
    public static void action(SlashCommandEvent event) {
        HashMap<String, UserGameData> data_list;

        int season = ((event.getOption("season") == null) ?
                ((LocalDateTime.now().getMonthValue() - 1) / 6) + 1 :
                (int) event.getOption("season").getAsLong());
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int month = ((event.getOption("month") == null) ?
                LocalDate.now().getMonthValue() :
                (int) event.getOption("month").getAsLong());
        int year = ((event.getOption("year") == null) ?
                LocalDate.now().getYear() :
                (int) event.getOption("year").getAsLong());

        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.getValidHalfDataPath(season, year)));
            data_list = (HashMap<String, UserGameData>) istream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            data_list = ScoreProcess.getUserDataList(start_month, year, end_month, year);
        }

        String finalName = getValidUser(event).getName();

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        GraphProcess graph = new GraphProcess();
        graph.scoreGraphGen(ScoreProcess.recentGameResult(finalName, start_month, year, end_month, year));

        int rank = getRank(data_list, finalName);

        File image = new File(Setting.GRAPH_PATH);
        event.replyEmbeds(
                getEmbed(
                        user,
                        String.format("[#%d] [%d.%dH] %s님의 통계", rank, year, season, user.name),
                        getValidUser(event).getEffectiveAvatarUrl()
                ).build()
        ).addFile(image, Setting.GRAPH_NAME).queue();
        Logger.addEvent(event);
    }
}
