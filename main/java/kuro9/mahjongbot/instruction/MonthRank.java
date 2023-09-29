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

    static String[] month_uma_id = {
            "month_rank_uma_go_first",
            "month_rank_uma_go_back",
            "month_rank_uma_refresh",
            "month_rank_uma_go_next",
            "month_rank_uma_go_last"
    };
    static String[] month_total_id = {
            "month_rank_totalgame_go_first",
            "month_rank_totalgame_go_back",
            "month_rank_totalgame_refresh",
            "month_rank_totalgame_go_next",
            "month_rank_totalgame_go_last"
    };
    static Button[] month_uma_button = {
            Button.secondary(month_uma_id[0], "<<"),
            Button.secondary(month_uma_id[1], "<"),
            Button.primary(month_uma_id[2], "F5"),
            Button.secondary(month_uma_id[3], ">"),
            Button.secondary(month_uma_id[4], ">>")
    };
    static Button[] month_total_game_button = {
            Button.secondary(month_total_id[0], "<<"),
            Button.secondary(month_total_id[1], "<"),
            Button.primary(month_total_id[2], "F5"),
            Button.secondary(month_total_id[3], ">"),
            Button.secondary(month_total_id[4], ">>")
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
                        String.format(resourceBundle.getString("rank.month.embed.summary.title"), year, month, filter),
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

        event.getHook().sendMessage(
                getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.month.embed.uma.title"), year, month, filter),
                        1,
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
        String buttonId = event.getInteraction().getComponentId();

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

        int finalPage = getNextPage(buttonId, month_uma_id, getPage(event), sorted_list.size());

        pageControl(
                event,
                month_uma_button,
                finalPage,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.month.embed.uma.title"), year, month, filter),
                        finalPage,
                        base64KeyGen(year, month, null, GameType.UMA, filter, finalPage, gameGroup)
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

        event.getHook().sendMessage(
                getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.month.embed.total_game_count.title"), year, month, filter),
                        1,
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
        String buttonId = event.getInteraction().getComponentId();

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

        int finalPage = getNextPage(buttonId, month_total_id, getPage(event), sorted_list.size());

        pageControl(
                event,
                month_total_game_button,
                finalPage,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.month.embed.total_game_count.title"), year, month, filter),
                        finalPage,
                        base64KeyGen(year, month, null, GameType.GMC, filter, finalPage, gameGroup)
                )
        );
        Logger.addEvent(event);
    }
}
