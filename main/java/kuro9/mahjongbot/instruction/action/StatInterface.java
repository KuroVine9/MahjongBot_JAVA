package kuro9.mahjongbot.instruction.action;

import kuro9.mahjongbot.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Optional;

public interface StatInterface {
    /**
     * 유저의 스탯 embed를 reply합니다.
     * @param event 해당 이벤트
     */
    default void action(SlashCommandInteractionEvent event) {}
}
