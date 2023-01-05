package kuro9.mahjongbot;

import com.opencsv.CSVWriter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ErrorLogger {
    private final String PATH;

    public ErrorLogger(String PATH) {
        this.PATH = PATH;
    }

    public void addErrorEvent(SlashCommandEvent event, String description, RestAction<User> admin) {
        ArrayList<String> log_list = new ArrayList<>();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]"));
        log_list.add(time);
        log_list.add(String.format("[%s]", description));
        log_list.add(String.format("%B", event.isFromGuild()));
        log_list.add(event.getUser().getAsTag());
        log_list.add(event.getName());
        event.getOptions().forEach(
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

        CSVWriter csv = null;
        try {
            csv = new CSVWriter(new FileWriter(PATH, true));
            String[] log = new String[log_list.size()];
            csv.writeNext(log_list.toArray(log));
            csv.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        embed.setFooter(time);
        embed.setColor(Color.RED);
        callAdmin(embed.build(), admin);
    }

    private void callAdmin(MessageEmbed embed, RestAction<User> admin) {
        admin.queue(
                user -> user.openPrivateChannel().queue(
                        privateChannel -> privateChannel.sendMessage(embed).queue()
                )
        );
    }
}
