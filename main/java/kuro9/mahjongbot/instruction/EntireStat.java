package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.*;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * 전체 범위의 유저 스탯을 출력합니다.
 */
public class EntireStat extends StatArranger implements StatInterface {
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        HashMap<String, UserGameData> data_list;
        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(Setting.USERDATA_PATH));
            data_list = (HashMap<String, UserGameData>) istream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            data_list = ScoreProcess.getUserDataList();
        }

        String finalName = getValidUser(event).getName();

        UserGameData user = Optional.ofNullable(data_list.get(finalName)).orElseGet(() -> new UserGameData(finalName));
        user.updateAllData();

        int rank = getRank(data_list, finalName);

        File image = generateGraph(ScoreProcess.recentGameResult(finalName));
        event.getHook().sendMessageEmbeds(
                getEmbed(
                        user,
                        String.format(resourceBundle.getString("entire_stat.embed.title"), rank, user.name),
                        getValidUser(event).getEffectiveAvatarUrl(),
                        event.getUserLocale()
                ).build()
        ).addFiles(FileUpload.fromData(image)).queue();
        Logger.addEvent(event);
    }
}
