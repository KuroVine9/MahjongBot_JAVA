package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

public class MonthStat extends StatArranger {
    public MonthStat(SlashCommandEvent event) {
        HashMap<String, UserGameData> data_list;

        int month = ((event.getOption("month") == null) ?
                LocalDate.now().getMonthValue() :
                (int) event.getOption("month").getAsLong());
        int year = ((event.getOption("year") == null) ?
                LocalDate.now().getYear() :
                (int) event.getOption("year").getAsLong());

        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.getValidMonthDataPath(month, year)));
            data_list = (HashMap<String, UserGameData>) istream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            data_list = ScoreProcess.getUserDataList(month, year);
        }
        // TODO 이름 공백
        String finalName = getValidUser(event).getName().replaceAll(" ", "");

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        GraphProcess graph = new GraphProcess();
        graph.scoreGraphGen(ScoreProcess.recentGameResult(finalName, month, year));

        var sorted_list = data_list.values().stream().sorted(
                (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
        ).toList();

        int rank = 0;
        for (; rank < sorted_list.size(); rank++) {
            if (sorted_list.get(rank).name.equals(finalName)) break;
        }

        File image = new File(Setting.GRAPH_PATH);
        event.replyEmbeds(
                getEmbed(user, rank + 1, getValidUser(event).getEffectiveAvatarUrl(), month, year).build()
        ).addFile(image, Setting.GRAPH_NAME).queue();
        Logger.addEvent(event);
    }
}
