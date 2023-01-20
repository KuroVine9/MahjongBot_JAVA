package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.HashMap;

public abstract class StatArranger {

    /**
     * 유효한 User 값을 반환합니다. <br>
     * {@code event}에 User 옵션값이 들어있다면 전달된 유저 값을, 없다면 명령어를 실행한 유저를 반환합니다.
     *
     * @param event 이벤트 매개변수
     * @return null이 아닌 유저 데이터
     */
    protected static User getValidUser(SlashCommandEvent event) {
        return ((event.getOption("user") == null) ?
                event.getUser() : event.getOption("user").getAsUser());
    }

    /**
     * 순위를 반환합니다.
     *
     * @param data_list 검색할 데이터 리스트
     * @param name      검색할 유저의 이름
     * @return 유저의 순위
     */
    protected static int getRank(HashMap<String, UserGameData> data_list, String name) {
        var sorted_list = data_list.values().stream().sorted(
                (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
        ).toList();

        int rank = 1;
        for (; rank < sorted_list.size(); rank++) {
            if (sorted_list.get(rank).name.equals(name)) break;
        }
        return rank;
    }

    /**
     * 출력할 임베드를 반환합니다.
     *
     * @param user      출력할 유저 데이터
     * @param title     출력할 임베드 메시지의 제목
     * @param thumb_url 유저의 썸네일 URL
     * @return 임베드 데이터
     */
    protected static EmbedBuilder getEmbed(UserGameData user, String title, String thumb_url) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setColor(Color.BLACK);
        embed.addField(
                "총 우마",
                (user.total_uma >= 0 ? "+" : "") + String.format("%.1f", user.total_uma),
                true
        );
        embed.addField(
                "총합 국 수",
                String.format("%s회", user.game_count),
                true
        );
        for (int i = 0; i < 4; i++) {
            embed.addField(
                    String.format("%d위률", i + 1),
                    String.format("%.2f%% (%d회)", user.rank_pp[i], user.rank_count[i]),
                    true
            );
        }
        embed.addField(
                "들통률",
                String.format("%.2f%% (%d회)", user.rank_pp[4], user.rank_count[4]),
                true
        );
        embed.addField(
                "평균순위",
                String.format("%.2f위", user.avg_rank),
                true
        );
        embed.addField(
                "평균우마",
                (user.avg_uma >= 0 ? "+" : "") + String.format("%.1f", user.avg_uma),
                true
        );
        embed.setImage(String.format("attachment://%s", Setting.GRAPH_NAME));
        embed.setThumbnail(thumb_url);
        return embed;
    }

}
