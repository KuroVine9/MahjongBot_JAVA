package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.data.UserGameData;
import kuro9.mahjongbot.data.UserGameDataComparatorKt;
import kuro9.mahjongbot.exception.DBConnectException;
import kuro9.mahjongbot.instruction.action.RankInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 1개월 범위의 순위표를 출력합니다.
 */
public class MonthRank extends RankArranger implements RankInterface {
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

    @Override
    public void summaryReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildId = getGuildID(event);
        String gameGroup = getGameGroup(event);

        HashMap<Long, UserGameData> userDataList;

        try {
            userDataList = DBScoreProcess.INSTANCE.getMonthUserData(guildId, month, year, gameGroup, filter);
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.getHook().sendMessageEmbeds(
                getSummaryEmbed(
                        String.format(resourceBundle.getString("month_rank.embed.summary.title"), year, month, filter),
                        userDataList.values().stream().toList(),
                        event.getUserLocale()
                ).build()
        ).queue();
        Logger.addEvent(event);
    }

    @Override
    public void umaReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildId = getGuildID(event);
        String gameGroup = getGameGroup(event);

        List<UserGameData> sorted_list = null;

        try {
            sorted_list = getMonthSortedList(
                    guildId,
                    gameGroup,
                    month,
                    year,
                    filter,
                    UserGameDataComparatorKt::compareWithUma
            );
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        month_uma_page_count[0] = 1;
        event.getHook().sendMessage(
                getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("month_rank.embed.uma.title"), year, month, filter),
                        month_uma_page_count[0],
                        base64KeyGen(year, month, null, GameType.UMA, filter, null, gameGroup)
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

    @Override
    public void umaPageControl(ButtonInteractionEvent event) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildId = getButtonGuildID(event);
        String gameGroup = getButtonGameGroup(event);

        List<UserGameData> sorted_list;

        try {
            sorted_list = getMonthSortedList(
                    guildId,
                    gameGroup,
                    month,
                    year,
                    filter,
                    UserGameDataComparatorKt::compareWithUma
            );
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        pageControl(
                event,
                month_uma_button,
                month_uma_page_count,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("month_rank.embed.uma.title"), year, month, filter),
                        month_uma_page_count[0],
                        base64KeyGen(year, month, null, GameType.UMA, filter, month_uma_page_count[0], gameGroup)
                )
        );
        Logger.addEvent(event);
    }

    @Override
    public void totalGameReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildId = getGuildID(event);
        String gameGroup = getGameGroup(event);

        List<UserGameData> sorted_list = null;

        try {
            sorted_list = getMonthSortedList(
                    guildId,
                    gameGroup,
                    month,
                    year,
                    filter,
                    UserGameDataComparatorKt::compareWithGameCount
            );
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        month_total_game_page_count[0] = 1;
        event.getHook().sendMessage(
                getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("month_rank.embed.total_game_count.title"), year, month, filter),
                        month_total_game_page_count[0],
                        base64KeyGen(year, month, null, GameType.GMC, filter, null, gameGroup)
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

    @Override
    public void totalGamePageControl(ButtonInteractionEvent event) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int month = getValidMonth(event);
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildId = getButtonGuildID(event);
        String gameGroup = getButtonGameGroup(event);

        List<UserGameData> sorted_list;

        try {
            sorted_list = getMonthSortedList(
                    guildId,
                    gameGroup,
                    month,
                    year,
                    filter,
                    UserGameDataComparatorKt::compareWithGameCount
            );
        }
        catch (DBConnectException e) {
            event.getHook()
                    .sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        pageControl(
                event,
                month_total_game_button,
                month_total_game_page_count,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("month_rank.embed.total_game_count.title"), year, month, filter),
                        month_total_game_page_count[0],
                        base64KeyGen(year, month, null, GameType.GMC, filter, month_total_game_page_count[0], gameGroup)
                )
        );
        Logger.addEvent(event);
    }
}
