package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.HeadlessGraphProcess;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.data.UserGameData;
import kuro9.mahjongbot.instruction.action.StatInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.ResourceBundle;

public abstract class StatArranger implements StatInterface {

    /**
     * 유효한 User 값을 반환합니다. <br>
     * {@code event}에 User 옵션값이 들어있다면 전달된 유저 값을, 없다면 명령어를 실행한 유저를 반환합니다.
     *
     * @param event 이벤트 매개변수
     * @return null이 아닌 유저 데이터
     */
    protected static User getValidUser(SlashCommandInteractionEvent event) {
        return ((event.getOption("user") == null) ?
                event.getUser() : event.getOption("user").getAsUser());
    }

    /**
     * 순위를 반환합니다.
     *
     * @param data_list 검색할 데이터 리스트
     * @param userID    검색할 유저의 ID
     * @return 유저의 순위
     */
    protected static int getRank(HashMap<Long, UserGameData> data_list, long userID) {
        var sorted_list = data_list.values().stream().sorted(
                (dataA, dataB) -> (int) ((dataB.getTotalUma() * 10) - (dataA.getTotalUma() * 10))
        ).toList();

        int rank = 0;
        for (; rank < sorted_list.size(); rank++) {
            if (sorted_list.get(rank).getId() == userID) return ++rank;
        }
        return -1;
    }

    /**
     * 출력할 임베드를 반환합니다.
     *
     * @param user      출력할 유저 데이터
     * @param title     출력할 임베드 메시지의 제목
     * @param thumb_url 유저의 썸네일 URL
     * @return 임베드 데이터
     */
    protected static EmbedBuilder getEmbed(UserGameData user, String title, String thumb_url, DiscordLocale locale) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(locale);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setColor(Color.BLACK);
        embed.addField(
                resourceBundle.getString("stat_arranger.embed.total_uma"),
                (user.getTotalUma() >= 0 ? "+" : "") + String.format("%.1f", user.getTotalUma()),
                true
        );
        embed.addField(
                resourceBundle.getString("stat_arranger.embed.total_game_count"),
                String.format(resourceBundle.getString("stat_arranger.embed.count_format"), user.getGameCount()),
                true
        );
        for (int i = 0; i < 4; i++) {
            embed.addField(
                    String.format(resourceBundle.getString("stat_arranger.embed.rank_pp"), i + 1),
                    String.format(resourceBundle.getString("stat_arranger.embed.pp_format"), user.getRankPercentage()[i], user.getRankPercentage()[i]),
                    true
            );
        }
        embed.addField(
                resourceBundle.getString("stat_arranger.embed.tobi"),
                String.format(resourceBundle.getString("stat_arranger.embed.pp_format"), user.getRankPercentage()[4], user.getRankPercentage()[4]),
                true
        );
        embed.addField(
                resourceBundle.getString("stat_arranger.embed.avg_rank"),
                String.format(resourceBundle.getString("stat_arranger.embed.rank_format"), user.getAvgRank()),
                true
        );
        embed.addField(
                resourceBundle.getString("stat_arranger.embed.avg_uma"),
                (user.getAvgUma() >= 0 ? "+" : "") + String.format("%.1f", user.getAvgUma()),
                true
        );
        embed.setImage(String.format("attachment://%s", Setting.GRAPH_NAME));
        embed.setThumbnail(thumb_url);
        return embed;
    }

    /**
     * 그래프 이미지를 반환합니다. 에러 등으로 인해 생성되지 않았을 경우에는 fall-back 이미지를 반환합니다.
     *
     * @param recent_data {@link HeadlessGraphProcess#scoreGraphGen(int[][])}의 파라미터
     * @return 그래프 이미지
     */
    protected static File generateGraph(int[][] recent_data) {
        HeadlessGraphProcess graph = new HeadlessGraphProcess();
        return new File(graph.scoreGraphGen(recent_data) ? Setting.GRAPH_PATH : Setting.FALLBACK_GRAPH_PATH);
    }

}
