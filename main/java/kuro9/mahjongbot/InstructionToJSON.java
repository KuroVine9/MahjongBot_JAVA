package kuro9.mahjongbot;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
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

        final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                .fromBundles("localization/Instructions", DiscordLocale.KOREAN, DiscordLocale.JAPANESE)
                .build();

        command_list.add(new String(Commands.slash("ping", "calc ping time of the bot")
                .setLocalizationFunction(localizationFunction).toData().toJson()));
        command_list.add(new String(Commands.slash("name", "print name")
                .setLocalizationFunction(localizationFunction)
                .addOptions(new OptionData(USER, "user", "user name to print", true))
                .toData().toJson()));
        command_list.add(new String(
                Commands.slash("msg", "msgtest")
                        .setLocalizationFunction(localizationFunction)
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("add", "add")
                        .setLocalizationFunction(localizationFunction)
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
                Commands.slash("entire_stat", "stat")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", "user")
                        )
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("month_stat", "month_stat")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", "user"),
                                new OptionData(INTEGER, "month", "month"),
                                new OptionData(INTEGER, "year", "year")
                        ).toData().toJson()));
        command_list.add(new String(
                Commands.slash("stat", "season_stat")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", "user"),
                                new OptionData(INTEGER, "season", "season")
                                        .addChoices(
                                                new Command.Choice("1-6", 1),
                                                new Command.Choice("7-12", 2)
                                        ),
                                new OptionData(INTEGER, "year", "year")
                        ).toData().toJson()));
        command_list.add(new String(
                Commands.slash("revalid", "revalid")
                        .setLocalizationFunction(localizationFunction)
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("entire_rank", "rank")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", "type"
                                ).addChoices(
                                        new Command.Choice("summary", 0),
                                        new Command.Choice("uma", 1),
                                        new Command.Choice("total_game_count", 2)
                                ),
                                new OptionData(INTEGER, "filter", "filter")
                        ).toData().toJson()
        ));
        command_list.add(new String(
                Commands.slash("month_rank", "month_rank")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", "type"
                                ).addChoices(
                                        new Command.Choice("summary", 0),
                                        new Command.Choice("uma", 1),
                                        new Command.Choice("total_game_count", 2)
                                ),
                                new OptionData(INTEGER, "month", "month"),
                                new OptionData(INTEGER, "year", "year"),
                                new OptionData(INTEGER, "filter", "filter")
                        ).toData().toJson()
        ));
        command_list.add(new String(
                Commands.slash("rank", "season_rank")
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", "type"
                                ).addChoices(
                                        new Command.Choice("summary", 0),
                                        new Command.Choice("uma", 1),
                                        new Command.Choice("total_game_count", 2)
                                ),
                                new OptionData(INTEGER, "season", "season")
                                        .addChoices(
                                                new Command.Choice("1-6", 1),
                                                new Command.Choice("7-12", 2)
                                        ),
                                new OptionData(INTEGER, "year", "year"),
                                new OptionData(INTEGER, "filter", "filter")
                        ).toData().toJson()
        ));
        Setting.init();
        PrintWriter ostream = new PrintWriter(new FileWriter(Setting.INST_PATH));
        ostream.println(
                command_list.stream().collect(Collectors.joining(",\n\t", "[\n\t", "\n]"))
        );
        ostream.close();

        System.out.println("[MahjongBot:InstructionToJSON] Instruction JSON Updated!");
    }
}
