package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Optional;

public class Stat extends StatArranger {
    public Stat(SlashCommandEvent event) {
        HashMap<String, UserGameData> data_list;
        ScoreProcess process = new ScoreProcess();

        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.USERDATA_PATH));
            data_list = (HashMap<String, UserGameData>) istream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            data_list = process.getUserDataList();
        }
        // TODO 이름 공백
        String finalName = getValidUser(event).getName().replaceAll(" ", "");

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        GraphProcess graph = new GraphProcess();
        graph.scoreGraphGen(process.recentGameResult(finalName));

        File image = new File(Setting.GRAPH_PATH);
        event.replyEmbeds(
                getEmbed(user, getValidUser(event).getEffectiveAvatarUrl()).build()
        ).addFile(image, Setting.GRAPH_NAME).queue();
        Logger.addEvent(event);
    }
}
