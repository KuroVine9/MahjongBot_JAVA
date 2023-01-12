package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Rank {
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

    public void summaryReply(SlashCommandEvent event) {
        ScoreProcess process = new ScoreProcess();
        HashMap<String, UserGameData> data_list = process.getUserDataList();
        List<UserGameData> sorted_list = data_list.values().stream().peek(UserGameData::updateAllData).filter(data -> data.game_count >= 10)
                .sorted(
                        (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
                ).toList();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("VRC 집계마작 순위 (10국 이상만 집계)");
        embed.addField(
                "총 우마(상위)",
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(0).name, sorted_list.get(0).total_uma,
                        sorted_list.get(1).name, sorted_list.get(1).total_uma,
                        sorted_list.get(2).name, sorted_list.get(2).total_uma
                ),
                true
        );
        embed.addField(
                "총 우마(하위)",
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
                "총합 국 수",
                String.format("%s : %d회\n%s : %d회\n%s : %d회",
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
                "들통률",
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
                "평균 순위(상위)",
                String.format("%s : %.2f\n%s : %.2f\n%s : %.2f",
                        sorted_list.get(0).name, sorted_list.get(0).avg_rank,
                        sorted_list.get(1).name, sorted_list.get(1).avg_rank,
                        sorted_list.get(2).name, sorted_list.get(2).avg_rank
                ),
                true
        );
        embed.addField(
                "평균 순위(하위)",
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
                "평균 우마(상위)",
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(0).name, sorted_list.get(0).avg_uma,
                        sorted_list.get(1).name, sorted_list.get(1).avg_uma,
                        sorted_list.get(2).name, sorted_list.get(2).avg_uma
                ),
                true
        );
        embed.addField(
                "평균 우마(하위)",
                String.format("%s : %+.1f\n%s : %+.1f\n%s : %+.1f",
                        sorted_list.get(sorted_list.size() - 1).name, sorted_list.get(sorted_list.size() - 1).avg_uma,
                        sorted_list.get(sorted_list.size() - 2).name, sorted_list.get(sorted_list.size() - 2).avg_uma,
                        sorted_list.get(sorted_list.size() - 3).name, sorted_list.get(sorted_list.size() - 3).avg_uma
                ),
                true
        );
        event.replyEmbeds(embed.build()).queue();
    }

    public void umaReply(SlashCommandEvent event) {
        uma_page_count[0] = 1;
        ScoreProcess process = new ScoreProcess();
        HashMap<String, UserGameData> data_list = process.getUserDataList();
        var sorted_list = data_list.values().stream().sorted(
                (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
        ).toList();
        event.reply(getUmaPrintString(sorted_list, uma_page_count[0])).addActionRow(uma_button).queue();
    }

    public void umaReply(ButtonClickEvent event) {
        ScoreProcess process = new ScoreProcess();
        HashMap<String, UserGameData> data_list = process.getUserDataList();
        var sorted_list = data_list.values().stream().sorted(
                (dataA, dataB) -> (int) ((dataB.total_uma * 100) - (dataA.total_uma * 100))
        ).toList();
        event.editMessage(getUmaPrintString(sorted_list, uma_page_count[0])).queue();
    }

    public void umaPageControl(ButtonClickEvent event) {
        pageControl(event, uma_button, uma_page_count, this::umaReply);
    }

    private String getUmaPrintString(List<UserGameData> data_list, int page) {
        StringBuilder page_block = new StringBuilder();
        page_block.append("```ansi\n").append(String.format("\u001B[1;34m총 우마 순위 (%d/%d)\u001B[0m\n\n", page, ((data_list.size() - 1) / 30) + 1));
        for (int i = (page - 1) * 30; i < Math.min(data_list.size(), page * 30); i++) {
            page_block.append(String.format("%-5d", i + 1)).append("\u001B[1;32m");
            page_block.append(getConstantWidthName(data_list.get(i).name));
            page_block.append("\u001B[0m");
            page_block.append(String.format("%+.1f\n", data_list.get(i).total_uma));
        }
        page_block.append("```");
        return page_block.toString();
    }

    public void totalGameReply(SlashCommandEvent event) {
        total_game_page_count[0] = 1;
        HashMap<String, UserGameData> data_list = new ScoreProcess().getUserDataList();
        var sorted_list = data_list.values().stream().peek(UserGameData::updateAllData).sorted(
                (dataA, dataB) -> dataB.game_count - dataA.game_count
        ).toList();
        event.reply(getTotalGamePrintString(sorted_list, total_game_page_count[0])).addActionRow(total_game_button).queue();
    }

    public void totalGameReply(ButtonClickEvent event) {
        ScoreProcess process = new ScoreProcess();
        HashMap<String, UserGameData> data_list = process.getUserDataList();
        var sorted_list = data_list.values().stream().peek(UserGameData::updateAllData).sorted(
                (dataA, dataB) -> (dataB.game_count - dataA.game_count)
        ).toList();
        event.editMessage(getTotalGamePrintString(sorted_list, total_game_page_count[0])).queue();
    }

    public void totalGamePageControl(ButtonClickEvent event) {
        pageControl(event, total_game_button, total_game_page_count, this::totalGameReply);
    }

    private String getTotalGamePrintString(List<UserGameData> data_list, int page) {
        StringBuilder page_block = new StringBuilder();
        page_block.append("```ansi\n").append(String.format("\u001B[1;34m총합 국 수 순위 (%d/%d)\u001B[0m\n\n", page, ((data_list.size() - 1) / 30) + 1));
        for (int i = (page - 1) * 30; i < Math.min(data_list.size(), page * 30); i++) {
            page_block.append(String.format("%-5d", i + 1)).append("\u001B[1;32m");
            page_block.append(getConstantWidthName(data_list.get(i).name));
            page_block.append("\u001B[0m");
            page_block.append(String.format("%d\n", data_list.get(i).game_count));
        }
        page_block.append("```");
        return page_block.toString();
    }

    private String getConstantWidthName(String name) {
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

    private int getLongCharCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (isLongChar(s.charAt(i))) count++;
        return count;
    }

    private boolean isLongChar(char ch) {
        // 한국어&&일본어
        return (('\u3131' <= ch && ch <= '\u3163') || ('\uAC00' <= ch && ch <= '\uD7A3') || ('\u3041' <= ch && ch <= '\u3096')
                || ('\u30A1' <= ch && ch <= '\u30FC') || ch == '\u3005' || ch == '\u3006' || ch == '\u3024'
                || ('\u4E00' <= ch && ch <= '\u9FA5'));
    }

    private void pageControl(ButtonClickEvent event, Button[] buttons, int[] page_count, Consumer<ButtonClickEvent> action) {
        if (event.getInteraction().getComponentId().equals(buttons[2].getId())) {
            action.accept(event);
            return;
        }
        else if (event.getInteraction().getComponentId().equals(buttons[0].getId())) {
            page_count[0] = 1;
        }
        else if (event.getInteraction().getComponentId().equals(buttons[1].getId()))
            if ((page_count[0] != 1)) --page_count[0];
            else ;
        else if (event.getInteraction().getComponentId().equals(buttons[3].getId()))
            if (page_count[0] < ((new ScoreProcess().getUserDataList().size() - 1) / 30 + 1))
                ++page_count[0];
            else ;
        else if (event.getInteraction().getComponentId().equals(buttons[4].getId()))
            page_count[0] = ((new ScoreProcess().getUserDataList().size() - 1) / 30 + 1);
        else return;
        action.accept(event);
    }
}