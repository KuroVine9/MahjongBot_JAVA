package kuro9.mahjongbot.data;

import java.util.HashMap;

public class PaiDiscardMachiData {
    public String pai;
    public int nokoru_pai;
    public HashMap<Character, int[]> machi;

    PaiDiscardMachiData(String pai) {
        this.pai = pai;
    }

    public PaiDiscardMachiData(int pai) {
        this.pai = switch (pai / 10) {
            case 0 -> String.format("%dm", pai % 10);
            case 1 -> String.format("%dp", pai % 10);
            case 2 -> String.format("%ds", pai % 10);
            default -> String.format("%dz", pai / 10 - 2);
        };
    }
}
