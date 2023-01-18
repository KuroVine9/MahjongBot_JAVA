package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Add {

    public Add(SlashCommandEvent event, RestAction<User> ADMIN) {
        if (!event.isFromGuild()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("403 Forbidden");
            embed.addField(
                    "정상적인 경로에서 명령어를 실행하여 주십시오.",
                    "에러 이벤트는 디버깅을 위해 저장됩니다.",
                    true
            );
            embed.setColor(Color.RED);
            event.replyEmbeds(embed.build()).queue();

            Logger.addErrorEvent(event, "not-guild-msg", ADMIN);
            return;
        }

        var options = event.getOptions();
        String[] names = new String[4];
        int[] scores = new int[4];
        for (int i = 0; i < options.size(); i++) {
            names[i / 2] = options.get(i).getAsUser().getName().replaceAll(" ", "");
            scores[i / 2] = (int) options.get(++i).getAsLong();
        }

        int result = ScoreProcess.addScore(names, scores);
        switch (result) {
            case -1 -> {     // PARAM ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("400 Bad Request");
                embed.setDescription("Parameter err.");
                embed.addField(
                        "다음의 항목을 확인해 주십시오.",
                        "``` - 점수의 총합\n - 점수의 정렬 여부\n - 중복된 이름```",
                        true
                );
                embed.setColor(Color.RED);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();

                Logger.addErrorEvent(event, "parameter-err", ADMIN);
            }
            case -2 -> {    // IOEXCEPTION
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("404 Not Found");
                embed.setDescription("File I/O Exception");
                embed.addField(
                        "순위 파일을 찾을 수 없습니다.",
                        "잠시 후 다시 시도해 주세요.",
                        true
                );
                embed.setColor(Color.RED);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();

                Logger.addErrorEvent(event, "file-not-found", ADMIN);
            }
            default -> {     // NO ERR
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("패보 기록완료!");
                for (int i = 0; i < 4; i++) {
                    embed.addField(
                            String.format("%d위 : %s", i + 1, names[i]),
                            String.valueOf(scores[i]),
                            true
                    );
                }
                embed.setFooter(
                        String.format(
                                "제 %d국, %s",
                                result,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        )
                );
                embed.setColor(Color.BLACK);
                event.replyEmbeds(embed.build()).queue();
                Logger.addEvent(event);
                ScoreProcess.revalidData();
            }
        }
    }
}
