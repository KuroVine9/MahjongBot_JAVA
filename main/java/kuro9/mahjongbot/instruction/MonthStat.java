package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

public class MonthStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
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

        String finalName = getValidUser(event).getName();

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        GraphProcess graph = new GraphProcess();
        graph.scoreGraphGen(ScoreProcess.recentGameResult(finalName, month, year));

        int rank = getRank(data_list, finalName);

        File image = new File(Setting.GRAPH_PATH);
        event.replyEmbeds(
                getEmbed(
                        user,
                        String.format("[#%d] [%d.%02d] %s님의 통계", rank, year, month, user.name),
                        getValidUser(event).getEffectiveAvatarUrl()
                ).build()
        ).addFiles(FileUpload.fromData(image)).queue();
        Logger.addEvent(event);
    }
}
