package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public abstract class StatArranger {

    /**
     * 유효한 User 값을 반환합니다. <br>
     * {@code event}에 User 옵션값이 들어있다면 전달된 유저 값을, 없다면 명령어를 실행한 유저를 반환합니다.
     *
     * @param event 이벤트 매개변수
     * @return null이 아닌 유저 데이터
     */
    protected User getValidUser(SlashCommandEvent event) {
        return ((event.getOption("user") == null) ?
                event.getUser() : event.getOption("user").getAsUser());
    }

    /**
     * 출력할 임베드를 반환합니다.
     *
     * @param user      출력할 유저 데이터
     * @param thumb_url 유저의 썸네일 URL
     * @param month     표시할 월
     * @param year      표시할 년도
     * @return 임베드 데이터
     */
    protected EmbedBuilder getEmbed(UserGameData user, String thumb_url, int month, int year) {
        EmbedBuilder embed = new EmbedBuilder();
        if (month == 0 || year == 0) embed.setTitle(String.format("%s님의 통계", user.name));
        else embed.setTitle(String.format("%d년 %d월 %s님의 통계", year, month, user.name));
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

    /**
     * 출력할 임베드를 반환합니다.
     *
     * @param user      출력할 유저 데이터
     * @param thumb_url 유저의 썸네일 URL
     * @return 임베드 데이터
     */
    protected EmbedBuilder getEmbed(UserGameData user, String thumb_url) {
        return getEmbed(user, thumb_url, 0, 0);
    }
}
