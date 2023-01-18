package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

public class Rank extends RankArranger {
    static int[] uma_page_count = {1};
    static Button[] uma_button = {
            Button.secondary("rank_uma_go_first", "<<"),
            Button.secondary("rank_uma_go_back", "<"),
            Button.primary("rank_uma_refresh", "F5"),
            Button.secondary("rank_uma_go_next", ">"),
            Button.secondary("rank_uma_go_last", ">>")
    };
    static int[] total_game_page_count = {1};
    static Button[] total_game_button = {
            Button.secondary("rank_totalgame_go_first", "<<"),
            Button.secondary("rank_totalgame_go_back", "<"),
            Button.primary("rank_totalgame_refresh", "F5"),
            Button.secondary("rank_totalgame_go_next", ">"),
            Button.secondary("rank_totalgame_go_last", ">>")
    };

    public static void summaryReply(SlashCommandEvent event) {
        event.replyEmbeds(
                getSummaryEmbed(
                        "VRC 집계마작 순위 (10국 이상)",
                        ScoreProcess.getUserDataList().values().stream().peek(UserGameData::updateAllData)
                                .filter(data -> data.game_count >= 10).toList()
                ).build()
        ).queue();
        Logger.addEvent(event);
    }

    public static void umaReply(SlashCommandEvent event) {
        var sorted_list = getSortedUmaList();
        uma_page_count[0] = 1;
        event.reply(
                getUmaPrintString(
                        sorted_list,
                        "총 우마 순위",
                        uma_page_count[0]
                )
        ).addActionRow(
                uma_button[0].asDisabled(),
                uma_button[1].asDisabled(),
                uma_button[2],
                sorted_list.size() > 30 ? uma_button[3] : uma_button[3].asDisabled(),
                sorted_list.size() > 30 ? uma_button[4] : uma_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    public static void umaPageControl(ButtonClickEvent event) {
        var sorted_list = getSortedUmaList();
        pageControl(
                event,
                uma_button,
                uma_page_count,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        "총 우마 순위",
                        uma_page_count[0]
                )
        );
        Logger.addEvent(event);
    }

    public static void totalGameReply(SlashCommandEvent event) {
        var sorted_list = getSortedTotalGameList();
        total_game_page_count[0] = 1;
        event.reply(
                getTotalGamePrintString(
                        sorted_list,
                        "총합 국 수 순위",
                        total_game_page_count[0]
                )
        ).addActionRow(
                total_game_button[0].asDisabled(),
                total_game_button[1].asDisabled(),
                total_game_button[2],
                sorted_list.size() > 30 ? total_game_button[3] : total_game_button[3].asDisabled(),
                sorted_list.size() > 30 ? total_game_button[4] : total_game_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    public static void totalGamePageControl(ButtonClickEvent event) {
        var sorted_list = getSortedTotalGameList();
        pageControl(
                event,
                total_game_button,
                total_game_page_count,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        "총합 국 수 순위",
                        total_game_page_count[0]
                )
        );
        Logger.addEvent(event);
    }
}