package kuro9.mahjongbot.tools;

import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.Setting;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;

import javax.annotation.PropertyKey;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class InstructionToJSON {
    public static void main(String[] args) throws IOException {
        ArrayList<String> command_list = new ArrayList<>();

        DiscordLocale[] supportLocales = ResourceHandler.getSupportLocale();
        ResourceBundle resourceKR = ResourceHandler.getResource(DiscordLocale.KOREAN);
        ResourceBundle resourceJP = ResourceHandler.getResource(DiscordLocale.JAPANESE);
        ResourceBundle resourceEN = ResourceHandler.getResource(DiscordLocale.ENGLISH_US);

        final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                .fromBundles("localization/Instructions", DiscordLocale.KOREAN, DiscordLocale.JAPANESE)
                .build();

        byte[] admin = Commands.slash("admin", resourceEN.getString("admin.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("add", resourceEN.getString("admin.add.description"))
                                .addOptions(
                                        new OptionData(
                                                USER,
                                                resourceEN.getString("admin.add.options.user.name"),
                                                resourceEN.getString("admin.add.options.user.description"),
                                                false
                                        )
                                ),
                        new SubcommandData("get", resourceEN.getString("admin.get.description")),
                        new SubcommandData("delete", resourceEN.getString("admin.delete.description"))
                                .addOptions(
                                        new OptionData(
                                                USER,
                                                resourceEN.getString("admin.delete.options.user.name"),
                                                resourceEN.getString("admin.delete.options.user.description"),
                                                false
                                        )
                                )

                ).toData().toJson();
        command_list.add(new String(admin));

        byte[] gameGroup = Commands.slash("game_group", resourceEN.getString("game_group.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("add", resourceEN.getString("game_group.add.description"))
                                .addOptions(
                                        new OptionData(STRING, "game_group", resourceEN.getString("game_group.add.options.description"))
                                ),
                        new SubcommandData("get", resourceEN.getString("game_group.get.description"))
                ).toData().toJson();
        command_list.add(new String(gameGroup));

        byte[] stat = Commands.slash("stat", resourceEN.getString("stat.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("entire", resourceEN.getString("stat.entire.description"))
                                .addOptions(
                                        new OptionData(USER, "user", resourceEN.getString("stat.entire.options.user.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("stat.entire.options.game_group.description"))
                                ),
                        new SubcommandData("season", resourceEN.getString("stat.season.description"))
                                .addOptions(
                                        new OptionData(USER, "user", resourceEN.getString("stat.season.options.user.description")),
                                        new OptionData(INTEGER, "season", resourceEN.getString("stat.season.options.season.description"))
                                                .addChoices(
                                                        new Command.Choice("1-6", 1),
                                                        new Command.Choice("7-12", 2)
                                                ),
                                        new OptionData(INTEGER, "year", resourceEN.getString("stat.season.options.year.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("stat.season.options.game_group.description"))
                                ),
                        new SubcommandData("month", resourceEN.getString("stat.month.description"))
                                .addOptions(
                                        new OptionData(USER, "user", resourceEN.getString("stat.month.options.user.description")),
                                        new OptionData(INTEGER, "month", resourceEN.getString("stat.month.options.month.description")),
                                        new OptionData(INTEGER, "year", resourceEN.getString("stat.month.options.year.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("stat.month.options.game_group.description"))
                                )

                ).toData().toJson();
        command_list.add(new String(stat));

        byte[] rank = Commands.slash("rank", resourceEN.getString("rank.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("entire", resourceEN.getString("rank.entire.description"))
                                .addOptions(
                                        new OptionData(
                                                INTEGER, "type", resourceEN.getString("rank.entire.options.type.description")
                                        ).addChoices(
                                                new Command.Choice("summary", 0),
                                                new Command.Choice("uma", 1),
                                                new Command.Choice("total_game_count", 2)
                                        ),
                                        new OptionData(INTEGER, "filter", resourceEN.getString("rank.entire.options.filter.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("rank.entire.options.game_group.description"))
                                ),
                        new SubcommandData("season", resourceEN.getString("rank.season.description"))
                                .addOptions(
                                        new OptionData(
                                                INTEGER, "type", resourceEN.getString("rank.season.options.type.description")
                                        ).addChoices(
                                                new Command.Choice("summary", 0)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.season.options.type.choices.summary.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.season.options.type.choices.summary.name"))
                                                ,
                                                new Command.Choice("uma", 1)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.season.options.type.choices.uma.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.season.options.type.choices.uma.name"))
                                                ,
                                                new Command.Choice("total_game_count", 2)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.season.options.type.choices.total_game_count.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.season.options.type.choices.total_game_count.name"))
                                        ),
                                        new OptionData(INTEGER, "season", resourceEN.getString("rank.season.options.season.description"))
                                                .addChoices(
                                                        new Command.Choice("1-6", 1)
                                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.season.options.season.choices.1-6.name"))
                                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.season.options.season.choices.1-6.name"))
                                                        ,
                                                        new Command.Choice("7-12", 2)
                                                                .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.season.options.season.choices.7-12.name"))
                                                                .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.season.options.season.choices.7-12.name"))

                                                ),
                                        new OptionData(INTEGER, "year", resourceEN.getString("rank.season.options.year.description")),
                                        new OptionData(INTEGER, "filter", resourceEN.getString("rank.season.options.filter.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("rank.season.options.game_group.description"))
                                ),
                        new SubcommandData("month", resourceEN.getString("rank.month.description"))
                                .addOptions(
                                        new OptionData(
                                                INTEGER, "type", resourceEN.getString("rank.month.options.type.description")
                                        ).addChoices(
                                                new Command.Choice("summary", 0)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.month.options.type.choices.summary.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.month.options.type.choices.summary.name"))
                                                ,
                                                new Command.Choice("uma", 1)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.month.options.type.choices.uma.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.month.options.type.choices.uma.name"))
                                                ,
                                                new Command.Choice("total_game_count", 2)
                                                        .setNameLocalization(DiscordLocale.KOREAN, resourceKR.getString("rank.month.options.type.choices.total_game_count.name"))
                                                        .setNameLocalization(DiscordLocale.JAPANESE, resourceJP.getString("rank.month.options.type.choices.total_game_count.name"))

                                        ),
                                        new OptionData(INTEGER, "month", resourceEN.getString("rank.month.options.month.description")),
                                        new OptionData(INTEGER, "year", resourceEN.getString("rank.month.options.year.description")),
                                        new OptionData(INTEGER, "filter", resourceEN.getString("rank.month.options.filter.description")),
                                        new OptionData(STRING, "game_group", resourceEN.getString("rank.month.options.game_group.description"))
                                )
                ).toData().toJson();
        command_list.add(new String(rank));

        byte[] modify = Commands.slash("modify", resourceEN.getString("modify.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addOptions(
                        new OptionData(USER, "game_id", resourceEN.getString("modify.options.game_id.description"), true),
                        new OptionData(USER, "1st_name", resourceEN.getString("modify.options.1st_name.description"), true),
                        new OptionData(INTEGER, "1st_score", resourceEN.getString("modify.options.1st_score.description"), true),
                        new OptionData(USER, "2nd_name", resourceEN.getString("modify.options.2nd_name.description"), true),
                        new OptionData(INTEGER, "2nd_score", resourceEN.getString("modify.options.2nd_score.description"), true),
                        new OptionData(USER, "3rd_name", resourceEN.getString("modify.options.3rd_name.description"), true),
                        new OptionData(INTEGER, "3rd_score", resourceEN.getString("modify.options.3rd_score.description"), true),
                        new OptionData(USER, "4th_name", resourceEN.getString("modify.options.4th_name.description"), true),
                        new OptionData(INTEGER, "4th_score", resourceEN.getString("modify.options.4th_score.description"), true)
                ).toData().toJson();
        command_list.add(new String(modify));

        byte[] delete = Commands.slash("delete", resourceEN.getString("delete.description"))
                .setLocalizationFunction(localizationFunction)
                .setGuildOnly(true)
                .addOption(USER, "game_id", resourceEN.getString("delete.options.game_id.description"), true)
                .toData().toJson();
        command_list.add(new String(delete));

        command_list.add(new String(Commands.slash("ping", "calc ping time of the bot")
                .setLocalizationFunction(localizationFunction).toData().toJson()));
        command_list.add(
                new String(
                        Commands.slash("file", "get file link").toData().toJson()
                )
        );
        command_list.add(
                new String(
                        Commands.slash("machi", resourceEN.getString("machi.description"))
                                .addOptions(
                                        new OptionData(STRING, "hand", resourceEN.getString("machi.options.hand.description"), true)
                                ).setLocalizationFunction(localizationFunction).toData().toJson()
                )
        );
        command_list.add(new String(
                Commands.slash("add", resourceEN.getString("add.description"))
                        .setLocalizationFunction(localizationFunction)
                        .setGuildOnly(true)
                        .addOptions(
                                new OptionData(USER, "1st_name", resourceEN.getString("add.options.1st_name.description"), true),
                                new OptionData(INTEGER, "1st_score", resourceEN.getString("add.options.1st_score.description"), true),
                                new OptionData(USER, "2nd_name", resourceEN.getString("add.options.2nd_name.description"), true),
                                new OptionData(INTEGER, "2nd_score", resourceEN.getString("add.options.2nd_score.description"), true),
                                new OptionData(USER, "3rd_name", resourceEN.getString("add.options.3rd_name.description"), true),
                                new OptionData(INTEGER, "3rd_score", resourceEN.getString("add.options.3rd_score.description"), true),
                                new OptionData(USER, "4th_name", resourceEN.getString("add.options.4th_name.description"), true),
                                new OptionData(INTEGER, "4th_score", resourceEN.getString("add.options.4th_score.description"), true),
                                new OptionData(STRING, "game_group", resourceEN.getString("add.options.game_group.description"))
                        )
                        .toData().toJson()));
        command_list.add(new String(
                Commands.slash("revalid", resourceEN.getString("revalid.description"))
                        .setLocalizationFunction(localizationFunction)
                        .toData().toJson()));

        Setting.parseString();
        PrintWriter ostream = new PrintWriter(new FileWriter(Setting.INST_PATH));
        ostream.println(
                command_list.stream().collect(Collectors.joining(",\n\t", "[\n\t", "\n]"))
        );
        ostream.close();

        System.out.println("[MahjongBot:InstructionToJSON] Instruction JSON Updated!");
    }

    private static Map<DiscordLocale, String> getLocalizeMap(@PropertyKey String key, DiscordLocale[] locales) {
        Map<DiscordLocale, String> map = new HashMap<>();
        Arrays.stream(locales).forEach(it -> {
            map.put(it, ResourceHandler.getResource(it).getString(key));
        });

        return map;
    }
}