package kuro9.mahjongbot;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class InstructionToJSON {
    public static void main(String[] args) throws IOException {
        ArrayList<String> command_list = new ArrayList<>();

        ResourceBundle resourceKR = ResourceHandler.getResource(DiscordLocale.KOREAN);
        ResourceBundle resourceJP = ResourceHandler.getResource(DiscordLocale.JAPANESE);
        ResourceBundle resourceEN = ResourceHandler.getResource(DiscordLocale.ENGLISH_US);

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
                Commands.slash("add", resourceEN.getString("add.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "1st_name", resourceEN.getString("add.options.1st_name.description"), true),
                                new OptionData(INTEGER, "1st_score", resourceEN.getString("add.options.1st_score.description"), true),
                                new OptionData(USER, "2nd_name", resourceEN.getString("add.options.2nd_name.description"), true),
                                new OptionData(INTEGER, "2nd_score", resourceEN.getString("add.options.2nd_score.description"), true),
                                new OptionData(USER, "3rd_name", resourceEN.getString("add.options.3rd_name.description"), true),
                                new OptionData(INTEGER, "3rd_score", resourceEN.getString("add.options.3rd_score.description"), true),
                                new OptionData(USER, "4th_name", resourceEN.getString("add.options.4th_name.description"), true),
                                new OptionData(INTEGER, "4th_score", resourceEN.getString("add.options.4th_score.description"), true)
                        )
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("entire_stat", resourceEN.getString("entire_stat.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", resourceEN.getString("entire_stat.options.user.description"))
                        )
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("month_stat", resourceEN.getString("month_stat.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", resourceEN.getString("month_stat.options.user.description")),
                                new OptionData(INTEGER, "month", resourceEN.getString("month_stat.options.month.description")),
                                new OptionData(INTEGER, "year", resourceEN.getString("month_stat.options.year.description"))
                        ).toData().toJson()));
        command_list.add(new String(
                Commands.slash("stat", resourceEN.getString("stat.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(USER, "user", resourceEN.getString("stat.options.user.description")),
                                new OptionData(INTEGER, "season", resourceEN.getString("stat.options.season.description"))
                                        .addChoices(
                                                new Command.Choice("1-6", 1)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("stat.options.season.choices.1-6.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("stat.options.season.choices.1-6.name")),
                                                new Command.Choice("7-12", 2)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("stat.options.season.choices.7-12.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("stat.options.season.choices.7-12.name"))
                                        ),
                                new OptionData(INTEGER, "year", resourceEN.getString("stat.options.year.description"))
                        ).toData().toJson()));
        command_list.add(new String(
                Commands.slash("revalid", resourceEN.getString("revalid.description"))
                        .setLocalizationFunction(localizationFunction)
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("entire_rank", resourceEN.getString("entire_rank.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", resourceEN.getString("entire_rank.options.type.description")
                                ).addChoices(
                                        new Command.Choice("summary", 0)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("entire_rank.options.type.choices.summary.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("entire_rank.options.type.choices.summary.name"))
                                        ,
                                        new Command.Choice("uma", 1)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("entire_rank.options.type.choices.uma.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("entire_rank.options.type.choices.uma.name"))
                                        ,
                                        new Command.Choice("total_game_count", 2)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("entire_rank.options.type.choices.total_game_count.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("entire_rank.options.type.choices.total_game_count.name"))

                                ),
                                new OptionData(INTEGER, "filter", resourceEN.getString("entire_rank.options.filter.description"))
                        ).toData().toJson()
        ));
        command_list.add(new String(
                Commands.slash("month_rank", resourceEN.getString("month_rank.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", resourceEN.getString("month_rank.options.type.description")
                                ).addChoices(
                                        new Command.Choice("summary", 0)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("month_rank.options.type.choices.summary.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("month_rank.options.type.choices.summary.name"))
                                        ,
                                        new Command.Choice("uma", 1)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("month_rank.options.type.choices.uma.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("month_rank.options.type.choices.uma.name"))
                                        ,
                                        new Command.Choice("total_game_count", 2)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("month_rank.options.type.choices.total_game_count.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("month_rank.options.type.choices.total_game_count.name"))

                                ),
                                new OptionData(INTEGER, "month", resourceEN.getString("month_rank.options.month.description")),
                                new OptionData(INTEGER, "year", resourceEN.getString("month_rank.options.year.description")),
                                new OptionData(INTEGER, "filter", resourceEN.getString("month_rank.options.filter.description"))
                        ).toData().toJson()
        ));
        command_list.add(new String(
                Commands.slash("rank", resourceEN.getString("rank.description"))
                        .setLocalizationFunction(localizationFunction)
                        .addOptions(
                                new OptionData(
                                        INTEGER, "type", resourceEN.getString("rank.options.type.description")
                                ).addChoices(
                                        new Command.Choice("summary", 0)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.options.type.choices.summary.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.options.type.choices.summary.name"))
                                        ,
                                        new Command.Choice("uma", 1)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.options.type.choices.uma.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.options.type.choices.uma.name"))
                                        ,
                                        new Command.Choice("total_game_count", 2)
                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.options.type.choices.total_game_count.name"))
                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.options.type.choices.total_game_count.name"))
                                ),
                                new OptionData(INTEGER, "season", resourceEN.getString("rank.options.season.description"))
                                        .addChoices(
                                                new Command.Choice("1-6", 1)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.options.season.choices.1-6.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.options.season.choices.1-6.name"))
                                                ,
                                                new Command.Choice("7-12", 2)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.options.season.choices.7-12.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.options.season.choices.7-12.name"))

                                        ),
                                new OptionData(INTEGER, "year", resourceEN.getString("rank.options.year.description")),
                                new OptionData(INTEGER, "filter", resourceEN.getString("rank.options.filter.description"))
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