package kuro9.mahjongbot;

import java.io.Serializable;
import java.util.Arrays;

public class UserGameData implements Serializable {
    public final String name;
    public double total_uma;
    public int game_count;
    public int[] rank_count;
    public double[] rank_pp;
    public double avg_rank;
    public double avg_uma;

    public UserGameData(String name) {
        this.name = name;
        rank_count = new int[]{0, 0, 0, 0, 0};
        rank_pp = new double[]{0, 0, 0, 0, 0};
        total_uma = 0;
        game_count = 0;
        avg_rank = 0;
        avg_uma = 0;
    }

    /**
     * 게임 결과를 현재 객체에 더합니다.
     *
     * @param score 점수
     * @param rank  순위(범위 : [1, 4])
     */
    public void addGameData(int score, int rank) {
        total_uma += (Math.round((((score - Setting.RETURN_POINT) / 1000.0) + Setting.UMA[rank - 1]) * 10) / 10.0);
        rank_count[rank - 1]++;
        if (score < 0) rank_count[4]++;
    }

    /**
     * 모든 필드를 업데이트합니다.
     */
    public void updateAllData() {
        total_uma = Math.round(total_uma * 10) / 10.0;
        game_count = Arrays.stream(rank_count).sum() - rank_count[4];

        avg_rank = 0;
        for (int i = 0; i < rank_pp.length - 1; i++) {
            rank_pp[i] = Math.round((double) rank_count[i] / (double) game_count * 10000) / 100.0;
            avg_rank += rank_count[i] * (i + 1);
        }
        rank_pp[4] = Math.round((double) rank_count[4] / (double) game_count * 10000) / 100.0;
        avg_rank = Math.round(avg_rank / game_count * 100) / 100.0;
        avg_uma = Math.round(total_uma / game_count * 10) / 10.0;
    }

    @Override
    public String toString() {
        updateAllData();
        return String.format(
                "{\"name\":\"%s\",\"total_uma\":%f,\"game_count\":%d,\"rank_count\":[%d,%d,%d,%d,%d],\"rank_pp\":[%f,%f,%f,%f,%f],\"avg_rank\":%f,\"avg_uma\":%f}",
                name,
                total_uma,
                game_count,
                rank_count[0], rank_count[1], rank_count[2], rank_count[3], rank_count[4],
                rank_pp[0], rank_pp[1], rank_pp[2], rank_pp[3], rank_pp[4],
                avg_rank,
                avg_uma
        );
    }
}
