package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import kuro9.mahjongbot.instruction.action.RankInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RankArranger implements RankInterface {

    protected static int getValidMonth(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("month") == null) ?
                    LocalDate.now().getMonthValue() :
                    (int) s.getOption("month").getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[\\d{4}.(\\d{2})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    protected static int getValidYear(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("year") == null) ?
                    LocalDate.now().getYear() :
                    (int) s.getOption("year").getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\[(\\d{4})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    protected static int getValidFilter(GenericInteractionCreateEvent event) {
        if (event instanceof SlashCommandInteractionEvent s) {
            return ((s.getOption("filter") == null) ?
                    0 : (int) s.getOption("filter").getAsLong());
        }
        else if (event instanceof ButtonInteractionEvent b) {
            String pattern = "\\([A-Za-z ]*(\\d+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(b.getMessage().getContentDisplay());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            else return 0;
        }
        else return 0;
    }

    protected static EmbedBuilder getSummaryEmbed(String title, List<UserGameData> sorted_list, DiscordLocale locale) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(locale);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        sorted_list = sorted_list.stream().sorted(
                (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
        ).toList();
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_uma_de"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(0).name, sorted_list.get(0).total_uma,
                        sorted_list.get(1).name, sorted_list.get(1).total_uma,
                        sorted_list.get(2).name, sorted_list.get(2).total_uma
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_uma_as"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(sorted_list.size() - 1).name, sorted_list.get(sorted_list.size() - 1).total_uma,
                        sorted_list.get(sorted_list.size() - 2).name, sorted_list.get(sorted_list.size() - 2).total_uma,
                        sorted_list.get(sorted_list.size() - 3).name, sorted_list.get(sorted_list.size() - 3).total_uma
                ),
                true
        );
        sorted_list = sorted_list.stream().sorted(
                (dataA, dataB) -> (int) (dataB.game_count - dataA.game_count)
        ).toList();
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.total_game_count.title"),
                String.format(resourceBundle.getString("rank_arranger.embed.total_game_count.field"),
                        sorted_list.get(0).name, sorted_list.get(0).game_count,
                        sorted_list.get(1).name, sorted_list.get(1).game_count,
                        sorted_list.get(2).name, sorted_list.get(2).game_count
                ),
                true
        );
        sorted_list = sorted_list.stream().sorted(
                (dataA, dataB) -> (int) ((dataB.rank_pp[4] * 100) - (dataA.rank_pp[4] * 100))
        ).toList();
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.tobi"),
                String.format("%s : %.1f%%\n%s : %.1f%%\n%s : %.1f%%",
                        sorted_list.get(0).name, sorted_list.get(0).rank_pp[4],
                        sorted_list.get(1).name, sorted_list.get(1).rank_pp[4],
                        sorted_list.get(2).name, sorted_list.get(2).rank_pp[4]
                ),
                true
        );
        sorted_list = sorted_list.stream().sorted(
                (dataA, dataB) -> (int) ((dataA.avg_rank * 100) - (dataB.avg_rank * 100))
        ).toList();
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_rank_de"),
                String.format("%s : %.2f\n%s : %.2f\n%s : %.2f",
                        sorted_list.get(0).name, sorted_list.get(0).avg_rank,
                        sorted_list.get(1).name, sorted_list.get(1).avg_rank,
                        sorted_list.get(2).name, sorted_list.get(2).avg_rank
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_rank_as"),
                String.format("%s : %.2f\n%s : %.2f\n%s : %.2f",
                        sorted_list.get(sorted_list.size() - 1).name, sorted_list.get(sorted_list.size() - 1).avg_rank,
                        sorted_list.get(sorted_list.size() - 2).name, sorted_list.get(sorted_list.size() - 2).avg_rank,
                        sorted_list.get(sorted_list.size() - 3).name, sorted_list.get(sorted_list.size() - 3).avg_rank
                ),
                true
        );
        sorted_list = sorted_list.stream().sorted(
                (dataA, dataB) -> (int) ((dataB.avg_uma * 100) - (dataA.avg_uma * 100))
        ).toList();
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_uma_de"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(0).name, sorted_list.get(0).avg_uma,
                        sorted_list.get(1).name, sorted_list.get(1).avg_uma,
                        sorted_list.get(2).name, sorted_list.get(2).avg_uma
                ),
                true
        );
        embed.addField(
                resourceBundle.getString("rank_arranger.embed.avg_uma_as"),
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(sorted_list.size() - 1).name, sorted_list.get(sorted_list.size() - 1).avg_uma,
                        sorted_list.get(sorted_list.size() - 2).name, sorted_list.get(sorted_list.size() - 2).avg_uma,
                        sorted_list.get(sorted_list.size() - 3).name, sorted_list.get(sorted_list.size() - 3).avg_uma
                ),
                true
        );
        return embed;
    }

    protected static List<UserGameData> getSortedTotalGameList(int filter) {
        return ScoreProcess.getUserDataList().values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (dataB.game_count - dataA.game_count)
                ).toList();
    }

    protected static List<UserGameData> getSortedTotalGameList(int filter, int month, int year) {
        return ScoreProcess.getUserDataList(month, year).values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (dataB.game_count - dataA.game_count)
                ).toList();
    }

    protected static List<UserGameData> getSortedTotalGameList(int filter, int start_month, int start_year, int end_month, int end_year) {
        return ScoreProcess.getUserDataList(start_month, start_year, end_month, end_year).values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (dataB.game_count - dataA.game_count)
                ).toList();
    }

    protected static List<UserGameData> getSortedTotalGameList() {
        return getSortedTotalGameList(0);
    }

    protected static String getTotalGamePrintString(List<UserGameData> data_list, String title, int page) {
        return getPrintString(
                data_list,
                title,
                page,
                data -> String.format("%d\n", data.game_count)
        );
    }

    protected static List<UserGameData> getSortedUmaList(int filter) {
        return ScoreProcess.getUserDataList().values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
                ).toList();
    }

    protected static List<UserGameData> getSortedUmaList(int filter, int month, int year) {
        return ScoreProcess.getUserDataList(month, year).values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
                ).toList();
    }

    protected static List<UserGameData> getSortedUmaList(int filter, int start_month, int start_year, int end_month, int end_year) {
        return ScoreProcess.getUserDataList(start_month, start_year, end_month, end_year).values().stream()
                .peek(UserGameData::updateAllData)
                .filter(data -> data.game_count >= filter)
                .sorted((dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
                ).toList();
    }

    protected static List<UserGameData> getSortedUmaList() {
        return getSortedUmaList(0);
    }

    protected static String getUmaPrintString(List<UserGameData> data_list, String title, int page) {
        return getPrintString(
                data_list,
                title,
                page,
                data -> String.format("%+.1f\n", data.total_uma)
        );
    }

    private static String getPrintString(List<UserGameData> data_list, String title, int page, Function<UserGameData, String> get_data) {
        StringBuilder page_block = new StringBuilder();
        page_block.append("```ansi\n").append(String.format("\u001B[1;34m%s (%d/%d)\u001B[0m\n\n", title, page, ((data_list.size() - 1) / 30) + 1));
        for (int i = (page - 1) * 30; i < Math.min(data_list.size(), page * 30); i++) {
            page_block.append(String.format("%-5d", i + 1)).append("\u001B[1;32m");
            page_block.append(getConstantWidthName(data_list.get(i).name));
            page_block.append("\u001B[0m");
            page_block.append(get_data.apply(data_list.get(i)));
        }
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
    protected static void pageControl(ButtonInteractionEvent event, Button[] buttons, int[] page_count, int size, Supplier<String> action) {
        event.deferEdit().queue();

        if (event.getInteraction().getComponentId().equals(buttons[2].getId())) {
            if ((page_count[0] == 1) && page_count[0] == ((size - 1) / 30 + 1)) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3].asDisabled(),
                        buttons[4].asDisabled()
                ).queue();
            }
            else if (page_count[0] == 1) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3],
                        buttons[4]
                ).queue();
            }
            else if (page_count[0] == ((size - 1) / 30 + 1)) {
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
            page_count[0] = 1;
            event.getHook().editOriginal(action.get()).setActionRow(
                    buttons[0].asDisabled(),
                    buttons[1].asDisabled(),
                    buttons[2],
                    buttons[3],
                    buttons[4]
            ).queue();
        }
        else if (event.getInteraction().getComponentId().equals(buttons[1].getId())) {
            if ((page_count[0] != 1)) --page_count[0];
            if (page_count[0] < 2) {
                event.getHook().editOriginal(action.get()).setActionRow(
                        buttons[0].asDisabled(),
                        buttons[1].asDisabled(),
                        buttons[2],
                        buttons[3],
                        buttons[4]
                ).queue();
            }
            else if (page_count[0] == ((size - 1) / 30 + 1)) {
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
            if (page_count[0] < ((size - 1) / 30 + 1)) ++page_count[0];
            if (page_count[0] > ((size - 1) / 30)) {
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
            page_count[0] = ((size - 1) / 30 + 1);
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
}
