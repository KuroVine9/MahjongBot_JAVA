package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class MonthRank extends RankArranger {
    static int[] month_uma_page_count = {1};
    static Button[] month_uma_button = {
            Button.secondary("month_rank_uma_go_first", "<<"),
            Button.secondary("month_rank_uma_go_back", "<"),
            Button.primary("month_rank_uma_refresh", "F5"),
            Button.secondary("month_rank_uma_go_next", ">"),
            Button.secondary("month_rank_uma_go_last", ">>")
    };
    static int[] month_total_game_page_count = {1};
    static Button[] month_total_game_button = {
            Button.secondary("month_rank_totalgame_go_first", "<<"),
            Button.secondary("month_rank_totalgame_go_back", "<"),
            Button.primary("month_rank_totalgame_refresh", "F5"),
            Button.secondary("month_rank_totalgame_go_next", ">"),
            Button.secondary("month_rank_totalgame_go_last", ">>")
    };

    public static void summaryReply(SlashCommandInteractionEvent event) {
        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);

        event.replyEmbeds(
                getSummaryEmbed(
                        String.format("[%d.%02d] VRC 집계마작 순위 (%d국 이상)", year, month, filter),
                        ScoreProcess.getUserDataList(month, year).values().stream().peek(UserGameData::updateAllData)
                                .filter(data -> data.game_count >= filter).toList()
                ).build()
        ).queue();
        Logger.addEvent(event);
    }

    public static void umaReply(SlashCommandInteractionEvent event) {
        var sorted_list = getSortedUmaList(getValidFilter(event), getValidMonth(event), getValidYear(event));
        month_uma_page_count[0] = 1;
        event.reply(
                getUmaPrintString(
                        sorted_list,
                        String.format("[%d.%02d] 총 우마 순위 (%d국 이상)", getValidYear(event), getValidMonth(event), getValidFilter(event)),
                        month_uma_page_count[0]
                )
        ).addActionRow(
                month_uma_button[0].asDisabled(),
                month_uma_button[1].asDisabled(),
                month_uma_button[2],
                sorted_list.size() > 30 ? month_uma_button[3] : month_uma_button[3].asDisabled(),
                sorted_list.size() > 30 ? month_uma_button[4] : month_uma_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    public static void umaPageControl(ButtonInteractionEvent event) {
        var sorted_list = getSortedUmaList(getValidFilter(event), getValidMonth(event), getValidYear(event));
        pageControl(
                event,
                month_uma_button,
                month_uma_page_count,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format("[%d.%02d] 총 우마 순위 (%d국 이상)", getValidYear(event), getValidMonth(event), getValidFilter(event)),
                        month_uma_page_count[0]
                )
        );
        Logger.addEvent(event);
    }

    public static void totalGameReply(SlashCommandInteractionEvent event) {
        var sorted_list = getSortedTotalGameList(getValidFilter(event), getValidMonth(event), getValidYear(event));
        month_total_game_page_count[0] = 1;
        event.reply(
                getTotalGamePrintString(
                        sorted_list,
                        String.format("[%d.%02d] 총합 국 수 순위 (%d국 이상)", getValidYear(event), getValidMonth(event), getValidFilter(event)),
                        month_total_game_page_count[0]
                )
        ).addActionRow(
                month_total_game_button[0].asDisabled(),
                month_total_game_button[1].asDisabled(),
                month_total_game_button[2],
                sorted_list.size() > 30 ? month_total_game_button[3] : month_total_game_button[3].asDisabled(),
                sorted_list.size() > 30 ? month_total_game_button[4] : month_total_game_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    public static void totalGamePageControl(ButtonInteractionEvent event) {
        var sorted_list = getSortedTotalGameList(getValidFilter(event), getValidMonth(event), getValidYear(event));
        pageControl(
                event,
                month_total_game_button,
                month_total_game_page_count,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format("[%d.%02d] 총합 국 수 순위 (%d국 이상)", getValidYear(event), getValidMonth(event), getValidFilter(event)),
                        month_total_game_page_count[0]
                )
        );
        Logger.addEvent(event);
    }
}
