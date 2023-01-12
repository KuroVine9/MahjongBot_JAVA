package kuro9.mahjongbot;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class InstructionToJSON {
    public static void main(String[] args) throws IOException, ParseException {
        ArrayList<String> command_list = new ArrayList<>();
        command_list.add(new String(new CommandData("ping", "calc ping time of the bot").toData().toJson()));
        command_list.add(new String(new CommandData("name", "print name")
                .addOptions(new OptionData(USER, "user", "user name to print", true))
                .toData().toJson()));
        command_list.add(new String(
                new CommandData("msg", "msgtest")
                        .toData().toJson()));
        command_list.add(new String(
                new CommandData("add", "add")
                        .addOptions(
                                new OptionData(USER, "1st_name", "test", true),
                                new OptionData(INTEGER, "1st_score", "test", true),
                                new OptionData(USER, "2nd_name", "test", true),
                                new OptionData(INTEGER, "2nd_score", "test", true),
                                new OptionData(USER, "3rd_name", "test", true),
                                new OptionData(INTEGER, "3rd_score", "test", true),
                                new OptionData(USER, "4th_name", "test", true),
                                new OptionData(INTEGER, "4th_score", "test", true)
                        )
                        .toData().toJson()));
        command_list.add(new String(
                new CommandData("stat", "stat")
                        .addOptions(
                                new OptionData(USER, "user", "user")
                        )
                        .toData().toJson()));
        command_list.add(new String(
                new CommandData("month_stat", "month_stat")
                        .addOptions(
                                new OptionData(USER, "user", "user"),
                                new OptionData(INTEGER, "month", "month"),
                                new OptionData(INTEGER, "year", "year")
                        ).toData().toJson()));
        command_list.add(new String(
                new CommandData("revalid", "revalid")
                        .toData().toJson()));
        command_list.add(new String(
                new CommandData("rank", "rank")
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", "type"
                                ).addChoices(
                                        new Command.Choice("summary", 0),
                                        new Command.Choice("uma", 1),
                                        new Command.Choice("total_game_count", 2),
                                        new Command.Choice("average_rank", 3),
                                        new Command.Choice("average_uma", 4)
                                )
                        ).toData().toJson()
        ));
        Setting.init();
        PrintWriter ostream = new PrintWriter(new FileWriter(Setting.INST_PATH));
        ostream.println(
                command_list.stream().collect(Collectors.joining(",\n\t", "[\n\t", "\n]"))
        );
        ostream.close();

        System.out.println("Instruction JSON Updated!");
    }
}
