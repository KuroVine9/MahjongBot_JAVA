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

import java.util.List;
import java.util.ResourceBundle;

public class SeasonRank extends RankArranger implements RankInterface {

    static String[] season_uma_id = {
            "season_rank_uma_go_first",
            "season_rank_uma_go_back",
            "season_rank_uma_refresh",
            "season_rank_uma_go_next",
            "season_rank_uma_go_last"
    };
    static String[] season_total_id = {
            "season_rank_totalgame_go_first",
            "season_rank_totalgame_go_back",
            "season_rank_totalgame_refresh",
            "season_rank_totalgame_go_next",
            "season_rank_totalgame_go_last"
    };

    static Button[] season_uma_button = {
            Button.secondary("season_rank_uma_go_first", "<<"),
            Button.secondary("season_rank_uma_go_back", "<"),
            Button.primary("season_rank_uma_refresh", "F5"),
            Button.secondary("season_rank_uma_go_next", ">"),
            Button.secondary("season_rank_uma_go_last", ">>")
    };
    static Button[] season_total_game_button = {
            Button.secondary("season_rank_totalgame_go_first", "<<"),
            Button.secondary("season_rank_totalgame_go_back", "<"),
            Button.primary("season_rank_totalgame_refresh", "F5"),
            Button.secondary("season_rank_totalgame_go_next", ">"),
            Button.secondary("season_rank_totalgame_go_last", ">>")
    };

    @Override
    public void summaryReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildID = getGuildID(event);
        String gameGroup = getGameGroup(event);

        try {
            event.getHook().sendMessageEmbeds(
                    getSummaryEmbed(
                            String.format(resourceBundle.getString("rank.season.embed.summary.title"), year, season, filter),
                            DBScoreProcess.INSTANCE.getSelectedUserData(guildID, start_month, year, end_month, year, gameGroup, filter)
                                    .values().stream().toList(),
                            event.getUserLocale()
                    ).build()
            ).queue();
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }
        Logger.addEvent(event);
    }

    @Override
    public void umaReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildID = getGuildID(event);
        String gameGroup = getGameGroup(event);

        List<UserGameData> sorted_list;
        try {
            sorted_list = getSelectedSortedList(
                    guildID,
                    start_month,
                    year,
                    end_month,
                    year,
                    gameGroup,
                    filter,
                    UserGameDataComparatorKt::compareWithUma
            );
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }

        event.getHook().sendMessage(
                getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.season.embed.uma.title"), year, season, filter),
                        1,
                        base64KeyGen(year, null, season, GameType.UMA, filter, null, gameGroup)
                )
        ).addActionRow(
                season_uma_button[0].asDisabled(),
                season_uma_button[1].asDisabled(),
                season_uma_button[2],
                sorted_list.size() > 30 ? season_uma_button[3] : season_uma_button[3].asDisabled(),
                sorted_list.size() > 30 ? season_uma_button[4] : season_uma_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    @Override
    public void umaPageControl(ButtonInteractionEvent event) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildID = getButtonGuildID(event);
        String gameGroup = getButtonGameGroup(event);
        int page = getPage(event);
        int gotoPage;
        String buttonId = event.getInteraction().getComponentId();

        List<UserGameData> sorted_list;
        try {
            sorted_list = getSelectedSortedList(
                    guildID,
                    start_month,
                    year,
                    end_month,
                    year,
                    gameGroup,
                    filter,
                    UserGameDataComparatorKt::compareWithUma
            );
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }

        if (buttonId.equals(season_uma_id[2])) gotoPage = page;
        else if (buttonId.equals(season_uma_id[0])) gotoPage = 1;
        else if (buttonId.equals(season_uma_id[4])) gotoPage = ((sorted_list.size() - 1) / 30 + 1);
        else if (buttonId.equals(season_uma_id[1])) gotoPage = Math.max(--page, 1);
        else if (buttonId.equals(season_uma_id[3])) gotoPage = Math.min(++page, ((sorted_list.size() - 1) / 30 + 1));
        else gotoPage = 1;

        int finalPage = gotoPage;

        pageControl(
                event,
                season_uma_button,
                finalPage,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.season.embed.uma.title"), year, season, filter),
                        finalPage,
                        base64KeyGen(year, null, season, GameType.UMA, filter, finalPage, gameGroup)
                )
        );
        Logger.addEvent(event);
    }

    @Override
    public void totalGameReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildID = getGuildID(event);
        String gameGroup = getGameGroup(event);

        List<UserGameData> sorted_list;
        try {
            sorted_list = getSelectedSortedList(
                    guildID,
                    start_month,
                    year,
                    end_month,
                    year,
                    gameGroup,
                    filter,
                    UserGameDataComparatorKt::compareWithGameCount
            );
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }

        event.getHook().sendMessage(
                getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.season.embed.total_game_count.title"), year, season, filter),
                        1,
                        base64KeyGen(year, null, season, GameType.GMC, filter, null, gameGroup)
                )
        ).addActionRow(
                season_total_game_button[0].asDisabled(),
                season_total_game_button[1].asDisabled(),
                season_total_game_button[2],
                sorted_list.size() > 30 ? season_total_game_button[3] : season_total_game_button[3].asDisabled(),
                sorted_list.size() > 30 ? season_total_game_button[4] : season_total_game_button[4].asDisabled()
        ).queue();
        Logger.addEvent(event);
    }

    @Override
    public void totalGamePageControl(ButtonInteractionEvent event) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);

        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);
        long guildID = getButtonGuildID(event);
        String gameGroup = getButtonGameGroup(event);
        int page = getPage(event);
        int gotoPage;
        String buttonId = event.getInteraction().getComponentId();

        List<UserGameData> sorted_list;
        try {
            sorted_list = getSelectedSortedList(
                    guildID,
                    start_month,
                    year,
                    end_month,
                    year,
                    gameGroup,
                    filter,
                    UserGameDataComparatorKt::compareWithGameCount
            );
        }
        catch (DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();
            return;
        }

        if (buttonId.equals(season_total_id[2])) gotoPage = page;
        else if (buttonId.equals(season_total_id[0])) gotoPage = 1;
        else if (buttonId.equals(season_total_id[4])) gotoPage = ((sorted_list.size() - 1) / 30 + 1);
        else if (buttonId.equals(season_total_id[1])) gotoPage = Math.max(--page, 1);
        else if (buttonId.equals(season_total_id[3])) gotoPage = Math.min(++page, ((sorted_list.size() - 1) / 30 + 1));
        else gotoPage = 1;

        int finalPage = gotoPage;

        pageControl(
                event,
                season_total_game_button,
                finalPage,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("rank.season.embed.total_game_count.title"), year, season, filter),
                        finalPage,
                        base64KeyGen(year, null, season, GameType.GMC, filter, finalPage, gameGroup)
                )
        );
        Logger.addEvent(event);
    }
}
