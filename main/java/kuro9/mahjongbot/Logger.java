package kuro9.mahjongbot;

import com.opencsv.CSVWriter;
import kuro9.mahjongbot.gdrive.GDrive;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 이벤트 로깅을 위한 클래스입니다.
 * 에러 이벤트는 {@code Setting.ADMIN}에 등록된 유저에게 알림을 보냅니다.
 */
public class Logger {

    /**
     * 일반 이벤트를 로깅합니다.
     *
     * @param event JDA의 SlashCommandEvent
     */
    public static void addEvent(GenericInteractionCreateEvent event) {
        writeLogToCSV(getLogList(event));
    }

    /**
     * 시스템 이벤트를 로깅합니다.
     *
     * @param description 이벤트 요약
     */
    public static void addSystemEvent(String description) {
        ArrayList<String> log_list = getSystemLogList(description);
        log_list.remove(1);
        writeLogToCSV(log_list);
    }

    /**
     * 시스템 명령어 에러 이벤트를 로깅합니다.
     * {@code callAdmin()}을 호출합니다.
     *
     * @param description 에러 형태에 대한 요약
     */
    public static void addSystemErrorEvent(String description) {
        ArrayList<String> log_list = getSystemLogList(description);
        writeErrorLogToCSV(log_list);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("NEW SYSTEM EVENT OCC.");
        embed.setDescription(description);
        embed.setFooter(log_list.get(0));
        embed.setColor(Color.RED);
        callAdmin(embed.build());
    }


    /**
     * 슬래시 명령어 에러 이벤트를 로깅합니다.
     * {@code callAdmin()}을 호출합니다.
     *
     * @param event       JDA의 SlashCommandEvent
     * @param description 에러 형태에 대한 요약
     */
    public static void addErrorEvent(SlashCommandInteractionEvent event, String description) {
        ArrayList<String> log_list = getLogList(event, description);

        writeErrorLogToCSV(log_list);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("NEW EVENT OCC.");
        embed.setDescription(description);
        embed.addField(
                "COMMAND_NAME",
                event.getName(),
                true
        );
        embed.addField(
                "USER_NAME",
                event.getUser().getAsTag(),
                true
        );
        embed.setFooter(log_list.get(0));
        embed.setColor(Color.RED);
        callAdmin(embed.build());
    }

    /**
     * 관리자를 다이렉트 메시지로 호출합니다.
     *
     * @param embed 전달할 메시지
     */
    private static void callAdmin(MessageEmbed embed) {
        if (Setting.ADMIN == null) return;
        Setting.ADMIN.queue(
                user -> user.openPrivateChannel().queue(
                        privateChannel -> privateChannel.sendMessageEmbeds(embed).queue()
                )
        );
    }

    /**
     * 로그를 작성합니다. 시스템 에러 로그용 메소드입니다.
     *
     * @param description 에러 형태에 대한 요약
     * @return 로그 메시지 리스트
     */
    private static ArrayList<String> getSystemLogList(String description) {
        ArrayList<String> log_list = new ArrayList<>();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]"));
        log_list.add(time);
        log_list.add(String.format("[%s]", description));
        log_list.add("NONE");
        log_list.add("SYSTEM");
        log_list.add("SYSTEM_START");
        return log_list;
    }

    /**
     * 로그를 작성합니다. 에러 로그용 메소드입니다.
     *
     * @param event       SlashCommandEvent, ButtonClickEvent의 슈퍼클래스 파라미터
     * @param description 에러 형태에 대한 요약
     * @return 로그 메시지 리스트
     */
    private static ArrayList<String> getLogList(GenericInteractionCreateEvent event, String description) {
        ArrayList<String> log_list = new ArrayList<>();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]"));
        log_list.add(time);
        log_list.add(String.format("[%s]", description));
        log_list.add(String.format("%B", event.isFromGuild()));
        log_list.add(event.getUser().getAsTag());

        if (event instanceof SlashCommandInteractionEvent s) {
            log_list.add(s.getName());
            s.getOptions().forEach(
                    option -> log_list.add(
                            String.format(
                                    "%s : %s",
                                    option.getName(),
                                    switch (option.getType()) {
                                        case STRING -> option.getAsString();
                                        case INTEGER -> String.valueOf(option.getAsLong());
                                        case BOOLEAN -> String.valueOf(option.getAsBoolean());
                                        case USER -> option.getAsUser().getAsTag();
                                        case ROLE -> option.getAsRole().getName();
                                        default -> null;
                                    }
                            )
                    )
            );
        }
        else if (event instanceof ButtonInteractionEvent b) {
            log_list.add(b.getButton().getId());
        }
        return log_list;
    }

    /**
     * 로그를 작성합니다. 일반 로그용 오버로딩입니다.
     *
     * @param event JDA의 SlashCommandEvent
     * @return 로그 메시지 리스트
     */
    private static ArrayList<String> getLogList(GenericInteractionCreateEvent event) {
        var list = getLogList(event, "");
        list.remove(1);
        return list;
    }

    /**
     * 에러 로그를 CSV 파일에 append합니다.
     *
     * @param log_list 로그 정보가 담긴 리스트
     */
    private static void writeErrorLogToCSV(ArrayList<String> log_list) {
        while (log_list.size() < 13) log_list.add("<NO_DATA>");
        abstractWriteLogToCSV(log_list, Setting.ERROR_LOG_PATH);
        System.out.printf("[MahjongBot:Logger] %s %s used %s\n", log_list.get(1), log_list.get(3), log_list.get(4));
        GDrive.upload(Setting.ERROR_LOG_FILE_ID, Setting.ERROR_LOG_PATH);
    }

    /**
     * 로그를 CSV 파일에 append합니다.
     *
     * @param log_list 로그 정보가 담긴 리스트
     */
    private static void writeLogToCSV(ArrayList<String> log_list) {
        while (log_list.size() < 12) log_list.add("<NO_DATA>");
        abstractWriteLogToCSV(log_list, Setting.LOG_PATH);
        System.out.printf("[MahjongBot:Logger] %s used %s\n", log_list.get(2), log_list.get(3));
        GDrive.upload(Setting.LOG_FILE_ID, Setting.LOG_PATH);
    }

    /**
     * 코드 재사용을 위한 메소드입니다. 로그를 PATH CSV 파일에 append합니다.
     *
     * @param log_list 로그 정보가 담긴 리스트
     * @param PATH     로그 파일 경로
     */
    private static void abstractWriteLogToCSV(ArrayList<String> log_list, String PATH) {
        CSVWriter csv;
        try {
            csv = new CSVWriter(new FileWriter(PATH, true));
            String[] log = new String[log_list.size()];
            csv.writeNext(log_list.toArray(log));
            csv.close();
        } catch (IOException e) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("NEW SYSTEM ERR OCC.");
            embed.setDescription("From Logger#abstractWriteLogToCSV(ArrayList<String>, String)");
            int count = 0;
            int page = 1;
            ArrayList<String> stack_msg = new ArrayList<>(10);
            for (StackTraceElement trace : e.getStackTrace()) {
                stack_msg.add(trace.toString());
                if (++count % 10 == 0) {
                    embed.addField(
                            String.format("Trace#%d", page++),
                            stack_msg.stream().collect(Collectors.joining("\n", "```\n", "```")),
                            false
                    );
                    stack_msg.clear();
                }
            }
            if (!stack_msg.isEmpty()) {
                embed.addField(
                        String.format("Trace#%d", page),
                        stack_msg.stream().collect(Collectors.joining("\n", "```\n", "```")),
                        false
                );
            }
            embed.setFooter("IOException");
            embed.setColor(Color.RED);
            callAdmin(embed.build());
            throw new RuntimeException(e);
        }
    }

}
