package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import kuro9.mahjongbot.instruction.action.RankInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * 전체 범위의 데이터 순위를 출력합니다.
 */
public class EntireRank extends RankArranger implements RankInterface {
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

    @Override
    public void summaryReply(SlashCommandInteractionEvent event) {
        int filter = getValidFilter(event);
        event.replyEmbeds(
                getSummaryEmbed(
                        String.format("VRC 집계마작 순위 (%d국 이상)", filter),
                        ScoreProcess.getUserDataList().values().stream().peek(UserGameData::updateAllData)
                                .filter(data -> data.game_count >= filter).toList()
                ).build()
        ).queue();
        Logger.addEvent(event);
    }

    @Override
    public void umaReply(SlashCommandInteractionEvent event) {
        int filter = getValidFilter(event);
        var sorted_list = getSortedUmaList(filter);
        uma_page_count[0] = 1;
        event.reply(
                getUmaPrintString(
                        sorted_list,
                        String.format("총 우마 순위 (%d국 이상)", filter),
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

    @Override
    public void umaPageControl(ButtonInteractionEvent event) {
        int filter = getValidFilter(event);
        var sorted_list = getSortedUmaList(filter);
        pageControl(
                event,
                uma_button,
                uma_page_count,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format("총 우마 순위 (%d국 이상)", filter),
                        uma_page_count[0]
                )
        );
        Logger.addEvent(event);
    }

    @Override
    public void totalGameReply(SlashCommandInteractionEvent event) {
        int filter = getValidFilter(event);
        var sorted_list = getSortedTotalGameList(filter);
        total_game_page_count[0] = 1;
        event.reply(
                getTotalGamePrintString(
                        sorted_list,
                        String.format("총합 국 수 순위 (%d국 이상)", filter),
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

    @Override
    public void totalGamePageControl(ButtonInteractionEvent event) {
        int filter = getValidFilter(event);
        var sorted_list = getSortedTotalGameList(filter);
        pageControl(
                event,
                total_game_button,
                total_game_page_count,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format("총합 국 수 순위 (%d국 이상)", filter),
                        total_game_page_count[0]
                )
        );
        Logger.addEvent(event);
    }
}