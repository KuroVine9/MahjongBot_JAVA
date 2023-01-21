package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Optional;

public class Stat extends StatArranger {
    public static void action(SlashCommandInteractionEvent event) {
        HashMap<String, UserGameData> data_list;
        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.USERDATA_PATH));
            data_list = (HashMap<String, UserGameData>) istream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            data_list = ScoreProcess.getUserDataList();
        }

        String finalName = getValidUser(event).getName();

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        GraphProcess graph = new GraphProcess();
        graph.scoreGraphGen(ScoreProcess.recentGameResult(finalName));

        int rank = getRank(data_list, finalName);

        File image = new File(Setting.GRAPH_PATH);
        event.replyEmbeds(
                getEmbed(
                        user,
                        String.format("[#%d] %s님의 통계", rank, user.name),
                        getValidUser(event).getEffectiveAvatarUrl()
                ).build()
        ).addFiles(FileUpload.fromData(image)).queue();
        Logger.addEvent(event);
    }
}
