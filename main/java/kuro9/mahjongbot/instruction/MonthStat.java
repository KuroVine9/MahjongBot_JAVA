package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.data.UserGameData;
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
import java.util.ResourceBundle;

public class MonthStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        HashMap<String, kuro9.mahjongbot.data.UserGameData> data_list;

        int month = ((event.getOption("month") == null) ?
                LocalDate.now().getMonthValue() :
                (int) event.getOption("month").getAsLong());
        int year = ((event.getOption("year") == null) ?
                LocalDate.now().getYear() :
                (int) event.getOption("year").getAsLong());

        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.getValidMonthDataPath(month, year)));
            data_list = (HashMap<String, kuro9.mahjongbot.data.UserGameData>) istream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            data_list = ScoreProcess.getUserDataList(month, year);
        }

        String finalName = getValidUser(event).getName();

        kuro9.mahjongbot.data.UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        int rank = getRank(data_list, finalName);

        File image = generateGraph(ScoreProcess.recentGameResult(finalName, month, year));
        event.getHook().sendMessageEmbeds(
                getEmbed(
                        user,
                        String.format(resourceBundle.getString("month_stat.embed.title"), rank, year, month, user.name),
                        getValidUser(event).getEffectiveAvatarUrl(),
                        event.getUserLocale()
                ).build()
        ).addFiles(FileUpload.fromData(image)).queue();
        Logger.addEvent(event);
    }
}
