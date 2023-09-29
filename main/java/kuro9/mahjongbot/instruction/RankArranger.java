package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.annotation.GuildRes;
import kuro9.mahjongbot.annotation.IntRange;
import kuro9.mahjongbot.data.UserGameData;
import kuro9.mahjongbot.data.UserGameDataComparatorKt;
import kuro9.mahjongbot.exception.DBConnectException;
import kuro9.mahjongbot.instruction.action.RankInterface;
import kuro9.mahjongbot.instruction.util.GameDataParse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class RankArranger extends GameDataParse implements RankInterface {

    protected static EmbedBuilder getSummaryEmbed(String title, List<UserGameData> userGameDataList, DiscordLocale locale) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(locale);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        userGameDataList.sort(UserGameDataComparatorKt::compareWithUma);
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_uma_de"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        userGameDataList.get(0).getUserName(), userGameDataList.get(0).getTotalUma(),
                        userGameDataList.get(1).getUserName(), userGameDataList.get(1).getTotalUma(),
                        userGameDataList.get(2).getUserName(), userGameDataList.get(2).getTotalUma()
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_uma_as"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        userGameDataList.get(userGameDataList.size() - 1).getUserName(), userGameDataList.get(userGameDataList.size() - 1).getTotalUma(),
                        userGameDataList.get(userGameDataList.size() - 2).getUserName(), userGameDataList.get(userGameDataList.size() - 2).getTotalUma(),
                        userGameDataList.get(userGameDataList.size() - 3).getUserName(), userGameDataList.get(userGameDataList.size() - 3).getTotalUma()
                ),
                true
        );
        userGameDataList.sort(UserGameDataComparatorKt::compareWithGameCount);
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_game_count.title"),
                String.format(resourceBundle.getString("rank_arranger.embed.total_game_count.field"),
                        userGameDataList.get(0).getUserName(), userGameDataList.get(0).getGameCount(),
                        userGameDataList.get(1).getUserName(), userGameDataList.get(1).getGameCount(),
                        userGameDataList.get(2).getUserName(), userGameDataList.get(2).getGameCount()
                ),
                true
        );
        userGameDataList.sort(UserGameDataComparatorKt::compareWithTobi);
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.tobi"),
                String.format("%s : %.1f%%\n%s : %.1f%%\n%s : %.1f%%",
                        userGameDataList.get(0).getUserName(), userGameDataList.get(0).getRankPercentage()[4],
                        userGameDataList.get(1).getUserName(), userGameDataList.get(1).getRankPercentage()[4],
                        userGameDataList.get(2).getUserName(), userGameDataList.get(2).getRankPercentage()[4]
                ),
                true
        );
        userGameDataList.sort(UserGameDataComparatorKt::compareWithAvgRank);
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_rank_de"),
                String.format("%s : %.2f\n%s : %.2f\n%s : %.2f",
                        userGameDataList.get(0).getUserName(), userGameDataList.get(0).getAvgRank(),
                        userGameDataList.get(1).getUserName(), userGameDataList.get(1).getAvgRank(),
                        userGameDataList.get(2).getUserName(), userGameDataList.get(2).getAvgRank()
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_rank_as"),
                String.format("%s : %.2f\n%s : %.2f\n%s : %.2f",
                        userGameDataList.get(userGameDataList.size() - 1).getUserName(), userGameDataList.get(userGameDataList.size() - 1).getAvgRank(),
                        userGameDataList.get(userGameDataList.size() - 2).getUserName(), userGameDataList.get(userGameDataList.size() - 2).getAvgRank(),
                        userGameDataList.get(userGameDataList.size() - 3).getUserName(), userGameDataList.get(userGameDataList.size() - 3).getAvgRank()
                ),
                true
        );
        userGameDataList.sort(UserGameDataComparatorKt::compareWithAvgUma);
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_uma_de"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        userGameDataList.get(0).getUserName(), userGameDataList.get(0).getAvgUma(),
                        userGameDataList.get(1).getUserName(), userGameDataList.get(1).getAvgUma(),
                        userGameDataList.get(2).getUserName(), userGameDataList.get(2).getAvgUma()
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_uma_as"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        userGameDataList.get(userGameDataList.size() - 1).getUserName(), userGameDataList.get(userGameDataList.size() - 1).getAvgUma(),
                        userGameDataList.get(userGameDataList.size() - 2).getUserName(), userGameDataList.get(userGameDataList.size() - 2).getAvgUma(),
                        userGameDataList.get(userGameDataList.size() - 3).getUserName(), userGameDataList.get(userGameDataList.size() - 3).getAvgUma()
                ),
                true
        );
        return embed;
    }

    protected static String getTotalGamePrintString(List<UserGameData> data_list, String title, int page, String base64Key) {
        return getPrintString(
                data_list,
                title,
                page,
                data -> String.format("%d\n", data.getGameCount()),
                base64Key
        );
    }

    protected static List<UserGameData> getAllSortedList(
            @GuildRes Long guildID,
            String gameGroup,
            int filterGameCount,
            Comparator<UserGameData> comparator
    ) throws DBConnectException {
        return DBScoreProcess.INSTANCE.getAllUserData(guildID, gameGroup, filterGameCount)
                .values().stream().sorted(comparator).toList();

    }

    protected static List<UserGameData> getMonthSortedList(
            @GuildRes Long guildID,
            String gameGroup,
            @IntRange(inclusiveStart = 1, inclusiveEnd = 12) int month,
            int year,
            int filterGameCount,
            Comparator<UserGameData> comparator
    ) throws DBConnectException {
        return DBScoreProcess.INSTANCE.getMonthUserData(guildID, month, year, gameGroup, filterGameCount)
                .values().stream().sorted(comparator).toList();
    }

    protected static List<UserGameData> getSelectedSortedList(
            @GuildRes Long guildID,
            @IntRange(inclusiveStart = 1, inclusiveEnd = 12) int startMonth,
            int startYear,
            @IntRange(inclusiveStart = 1, inclusiveEnd = 12) int endMonth,
            int endYear,
            String gameGroup,
            int filterGameCount,
            Comparator<UserGameData> comparator
    ) throws DBConnectException {
        return DBScoreProcess.INSTANCE.getSelectedUserData(
                guildID,
                startMonth,
                startYear,
                endMonth,
                endYear,
                gameGroup,
                filterGameCount
        ).values().stream().sorted(comparator).toList();
    }

    protected static String getUmaPrintString(List<UserGameData> data_list, String title, int page, String base64Key) {
        return getPrintString(
                data_list,
                title,
                page,
                data -> String.format("%+.1f\n", data.getTotalUma()),
                base64Key
        );
    }

    /**
     * @param data_list
     * @param title
     * @param page
     * @param get_data
     * @param base64Key yyyy-mm-s-{game-type}-{filter}-{page}-{game-group}
     *                  y = 년도(null시 0000), m = 월(null시 00), s = 시즌(null시 0),
     *                  game-type = {"SUM" | "UMA" | "GMC"} (요약, 우마, 총합 국 수),
     *                  filter = 국 수 필터, page = 현재 페이지, game-group = 게임 그룹(null시 "")
     */
    private static String getPrintString(List<UserGameData> data_list, String title, int page, Function<UserGameData, String> get_data, String base64Key) {
        StringBuilder page_block = new StringBuilder();
        page_block.append("```ansi\n").append(String.format("\u001B[1;34m%s (%d/%d)\u001B[0m\n\n", title, page, ((data_list.size() - 1) / 30) + 1));
        for (int i = (page - 1) * 30; i < Math.min(data_list.size(), page * 30); i++) {
            page_block.append(String.format("%-5d", i + 1)).append("\u001B[1;32m");
            page_block.append(getConstantWidthName(data_list.get(i).getUserName()));
            page_block.append("\u001B[0m");
            page_block.append(get_data.apply(data_list.get(i)));
        }
        page_block.append(String.format("\n\n\u001B[0;30mkey=%s\u001B[0m", base64Key));
        page_block.append("```");
        return page_block.toString();
    }

    protected static String getConstantWidthName(String name) {
        StringBuilder line = new StringBuilder();
        if (name.length() + getLongCharCount(name) > 20) {
            int space_count = 0;
            int char_count = 0;
            for (; (char_count < name.length() && space_count <= 20); char_count++) {
                if (isLongChar(name.charAt(char_count))) space_count += 2;
                else space_count++;
            }
            if (char_count < name.length()) name = name.substring(0, char_count);
        }
        line.append(name);
        line.append(" ".repeat(Math.max(0, 24 - (name.length() + getLongCharCount(name)))));

        return line.toString();
    }

    protected static int getLongCharCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (isLongChar(s.charAt(i))) count++;
        return count;
    }

    protected static boolean isLongChar(char ch) {
        // 한국어&&일본어
        return (('\u3131' <= ch && ch <= '\u3163') || ('\uAC00' <= ch && ch <= '\uD7A3') || ('\u3041' <= ch && ch <= '\u3096')
                || ('\u30A1' <= ch && ch <= '\u30FC') || ch == '\u3005' || ch == '\u3006' || ch == '\u3024'
                || ('\u4E00' <= ch && ch <= '\u9FA5'));
    }

    /**
     * Rank의 message page를 버튼으로 컨트롤하는 메소드입니다.
     * 연산 시간을 위해 deferEdit() 메소드를 사용합니다.
     *
     * @param event      버튼 인터렉션 이벤트
     * @param buttons    추가할 버튼 배열(0: 맨 앞, 1: 앞으로, 2: 새로고침, 3: 뒤로, 4: 맨 뒤로)
     * @param page_count 현재 페이지 인덱스.
     * @param size       전체 리스트의 항목 수 (30줄마다 한 페이지로 생성)
     * @param action     출력할 페이지의 String을 리턴하는 함수
     */
    protected static void pageControl(ButtonInteractionEvent event, Button[] buttons, int page_count, int size, Supplier<String> action) {
        event.deferEdit().queue();

        if (event.getInteraction().getComponentId().equals(buttons[2].getId())) {
            if ((page_count == 1) && page_count == ((size - 1) / 30 + 1)) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3].asDisabled(),
                        buttons[4].asDisabled()
                ).queue();
            }
            else if (page_count == 1) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3],
                        buttons[4]
                ).queue();
            }
            else if (page_count == ((size - 1) / 30 + 1)) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0],
                        buttons[1],
                        buttons[2],
                        buttons[3].asDisabled(),
                        buttons[4].asDisabled()
                ).queue();
            }
            else event.getHook().editOriginal(action.get()).setActionRow(buttons).queue();
            return;
        }
        else if (event.getInteraction().getComponentId().equals(buttons[0].getId())) {
            event.getHook().editOriginal(action.get()).setActionRow(
                    buttons[0].asDisabled(),
                    buttons[1].asDisabled(),
                    buttons[2],
                    buttons[3],
                    buttons[4]
            ).queue();
        }
        else if (event.getInteraction().getComponentId().equals(buttons[1].getId())) {
            // if ((page_count[0] != 1)) --page_count[0];
            if (page_count < 2) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3],
                        buttons[4]
                ).queue();
            }
            else if (page_count == ((size - 1) / 30 + 1)) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0],
                        buttons[1],
                        buttons[2],
                        buttons[3].asDisabled(),
                        buttons[4].asDisabled()
                ).queue();
            }
            else {
                event.getHook().editOriginal(action.get()).setActionRow(buttons).queue();
            }

        }
        else if (event.getInteraction().getComponentId().equals(buttons[3].getId())) {
            // if (page_count[0] < ((size - 1) / 30 + 1)) ++page_count[0];
            if (page_count > ((size - 1) / 30)) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0],
                        buttons[1],
                        buttons[2],
                        buttons[3].asDisabled(),
                        buttons[4].asDisabled()
                ).queue();
            }
            else {
                event.getHook().editOriginal(action.get()).setActionRow(buttons).queue();
            }

        }
        else if (event.getInteraction().getComponentId().equals(buttons[4].getId())) {
            // page_count[0] = ((size - 1) / 30 + 1);
            event.getHook().editOriginal(action.get()).setActionRow(
                    buttons[0],
                    buttons[1],
                    buttons[2],
                    buttons[3].asDisabled(),
                    buttons[4].asDisabled()
            ).queue();
        }
        else return;
    }

    protected int getNextPage(String buttonId, String[] buttons, int nowPage, int dataSize) {
        System.out.println(("param: nowpage=" + nowPage + ", datasize=" + dataSize));
        if (buttonId.equals(buttons[2])) return nowPage;
        else if (buttonId.equals(buttons[0])) return 1;
        else if (buttonId.equals(buttons[4])) return ((dataSize - 1) / 30 + 1);
        else if (buttonId.equals(buttons[1])) return Math.max(--nowPage, 1);
        else if (buttonId.equals(buttons[3])) return Math.min(++nowPage, ((dataSize - 1) / 30 + 1));
        else throw new IllegalStateException("getNextPage param err! nowpage=" + nowPage + ", datasize=" + dataSize);
    }

    /**
     * @return yyyy-mm-s-{game-type}-{filter}-{page}-{game-group}
     * y = 년도(null시 0000), m = 월(null시 00), s = 시즌(null시 0),
     * game-type = {"SUM" | "UMA" | "GMC"} (요약, 우마, 총합 국 수),
     * filter = 국 수 필터, page = 현재 페이지, game-group = 게임 그룹(null시 "")
     */
    protected String base64KeyGen(
            @Nullable Integer year,
            @Nullable Integer month,
            @Nullable Integer season,
            @Nonnull GameType gameType,
            @Nullable Integer filter,
            @Nullable Integer page,
            @Nullable String gameGroup
    ) {
        String original = String.format(
                "%s-%s-%s-%s-%s-%s-%s",
                (year == null) ? "0000" : year.toString(),
                (month == null) ? "00" : month.toString(),
                (season == null) ? "0" : season.toString(),
                gameType.name(),
                (filter == null) ? "0" : filter.toString(),
                (page == null) ? "1" : page.toString(),
                (gameGroup == null) ? "" : gameGroup
        );
        return Base64.getEncoder().encodeToString(original.getBytes());
    }

    /*요약, 우마, 총합 국 수*/
    enum GameType {SUM, UMA, GMC}
}
