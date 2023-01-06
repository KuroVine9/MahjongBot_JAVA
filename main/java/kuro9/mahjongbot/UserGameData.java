package kuro9.mahjongbot;

import java.io.Serializable;
import java.util.Arrays;

public class UserGameData implements Serializable {
    final String name;
    double total_uma;
    int game_count;
    int[] rank_count;
    double[] rank_pp;
    double avg_rank;
    double avg_uma;

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
        return String.format(
                "[%s] [T_U : %f] [T_G : %d] [%s]", name, total_uma, game_count, Arrays.toString(rank_count)
        );
    }
}
