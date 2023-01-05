package kuro9.mahjongbot;

import java.util.Arrays;

public class UserGameData {
    final String name;
    double total_uma;
    int game_count;
    int[] rank_count;
    double[] rank_pp;
    double avg_rank;
    double avg_uma;

    /**
     * 반환점
     */
    static int return_point;

    /**
     * 1등-4등에게 더해지는 우마
     */
    static int[] uma;

    public UserGameData(String name) {
        this.name = name;
        rank_count = new int[]{0, 0, 0, 0, 0};
        rank_pp = new double[]{0, 0, 0, 0, 0};
        total_uma = 0;
        game_count = 0;
        avg_rank = 0;
        avg_uma = 0;
    }

    public void addGameData(int score, int rank) {
        total_uma += scoreToUma(score, rank);
        rank_count[rank - 1]++;
    }

    public void updateAllData() {
        total_uma = Math.round(total_uma * 10) / 10.0;
        game_count = Arrays.stream(rank_count).sum();

        avg_rank = 0;
        for (int i = 0; i < rank_pp.length; i++) {
            rank_pp[i] = Math.round((double) rank_count[i] / (double) game_count * 1000) / 10.0;
            avg_rank += rank_count[i] * (i + 1);
        }
        avg_rank = Math.round(avg_rank / game_count * 100) / 100.0;
        avg_uma = Math.round(total_uma / game_count * 10) / 10.0;
    }

    private double scoreToUma(int score, int rank) {
        double jun_uma = ((score - return_point) / 1000.0) + uma[rank - 1];
        return Math.round(jun_uma * 10) / 10.0;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] [T_U : %f] [T_G : %d] [%s]", name, total_uma, game_count, Arrays.toString(rank_count)
        );
    }
}
