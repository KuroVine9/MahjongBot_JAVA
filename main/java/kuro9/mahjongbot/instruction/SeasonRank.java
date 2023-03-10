package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import kuro9.mahjongbot.instruction.action.RankInterface;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeasonRank extends RankArranger implements RankInterface {

    static int[] season_uma_page_count = {1};
    static Button[] season_uma_button = {
            Button.secondary("season_rank_uma_go_first", "<<"),
            Button.secondary("season_rank_uma_go_back", "<"),
            Button.primary("season_rank_uma_refresh", "F5"),
            Button.secondary("season_rank_uma_go_next", ">"),
            Button.secondary("season_rank_uma_go_last", ">>")
    };
    static int[] season_total_game_page_count = {1};
    static Button[] season_total_game_button = {
            Button.secondary("season_rank_totalgame_go_first", "<<"),
            Button.secondary("season_rank_totalgame_go_back", "<"),
            Button.primary("season_rank_totalgame_refresh", "F5"),
            Button.secondary("season_rank_totalgame_go_next", ">"),
            Button.secondary("season_rank_totalgame_go_last", ">>")
    };

    private int getValidSeason(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("season") == null) ?
                    ((LocalDateTime.now().getMonthValue() - 1) / 6) + 1 :
                    (int) s.getOption("season").getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[\\d{4}.(\\d)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    @Override
    public void summaryReply(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        int season = getValidSeason(event);
        int start_month = season * 6 - 5;
        int end_month = season * 6;
        int year = getValidYear(event);
        int filter = getValidFilter(event);

        event.getHook().sendMessageEmbeds(
                getSummaryEmbed(
                        String.format(resourceBundle.getString("season_rank.embed.summary.title"), year, season, filter),
                        ScoreProcess.getUserDataList(start_month, year, end_month, year).values().stream().peek(UserGameData::updateAllData)
                                .filter(data -> data.game_count >= filter).toList(),
                        event.getUserLocale()
                ).build()
        ).queue();
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

        var sorted_list = getSortedUmaList(filter, start_month, year, end_month, year);
        season_uma_page_count[0] = 1;
        event.getHook().sendMessage(
                getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("season_rank.embed.uma.title"), year, season, filter),
                        season_uma_page_count[0]
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

        var sorted_list = getSortedUmaList(filter, start_month, year, end_month, year);
        pageControl(
                event,
                season_uma_button,
                season_uma_page_count,
                sorted_list.size(),
                () -> getUmaPrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("season_rank.embed.uma.title"), year, season, filter),
                        season_uma_page_count[0]
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

        var sorted_list = getSortedTotalGameList(filter, start_month, year, end_month, year);
        season_total_game_page_count[0] = 1;
        event.getHook().sendMessage(
                getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("season_rank.embed.total_game_count.title"), year, season, filter),
                        season_total_game_page_count[0]
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

        var sorted_list = getSortedTotalGameList(filter, start_month, year, end_month, year);
        pageControl(
                event,
                season_total_game_button,
                season_total_game_page_count,
                sorted_list.size(),
                () -> getTotalGamePrintString(
                        sorted_list,
                        String.format(resourceBundle.getString("season_rank.embed.total_game_count.title"), year, season, filter),
                        season_total_game_page_count[0]
                )
        );
        Logger.addEvent(event);
    }
}
