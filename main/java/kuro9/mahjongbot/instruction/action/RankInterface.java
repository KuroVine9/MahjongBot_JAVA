package kuro9.mahjongbot.instruction.action;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ScoreProcess;
import kuro9.mahjongbot.UserGameData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import static kuro9.mahjongbot.instruction.RankArranger.*;

public interface RankInterface {
    /**
     * 순위 요약 임베드를 reply합니다.
     * @param event 해당 이벤트
     */
    default void summaryReply(SlashCommandInteractionEvent event) {}

    /**
     * 우마 순위표를 reply합니다.
     * @param event 해당 이벤트
     */
    default void umaReply(SlashCommandInteractionEvent event) {}

    /**
     * 우마 순위표 버튼 컨트롤 유닛입니다.
     * @param event 버튼 이벤트
     */
    default void umaPageControl(ButtonInteractionEvent event) {}
    /**
     * 대국수 순위표를 reply합니다.
     * @param event 해당 이벤트
     */
    default void totalGameReply(SlashCommandInteractionEvent event) {}
    /**
     * 대국수 순위표 버튼 컨트롤 유닛입니다.
     * @param event 버튼 이벤트
     */
    default void totalGamePageControl(ButtonInteractionEvent event) {}
}
