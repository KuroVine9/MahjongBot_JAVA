package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.annotation.GuildRes;
import kuro9.mahjongbot.annotation.UserRes;
import kuro9.mahjongbot.db.DBHandler;
import kuro9.mahjongbot.db.data.Game;
import kuro9.mahjongbot.db.data.GameResult;
import kuro9.mahjongbot.exception.DBConnectException;
import kuro9.mahjongbot.exception.GameGroupNotFoundException;
import kuro9.mahjongbot.exception.ParameterErrorException;
import kuro9.mahjongbot.instruction.util.GameDataParse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

/**
 * 순위 데이터 파일에 기록을 추가합니다.
 */
public class AddScore extends GameDataParse {

    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        if (!event.isFromGuild()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("403 Forbidden");
            embed.addField(
                    resourceBundle.getString("exception.not_in_guild.title"),
                    resourceBundle.getString("exception.not_in_guild.description"),
                    true
            );
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).queue();

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG);
            return;
        }

        int[] scores = {
                event.getOption("1st_score").getAsInt(),
                event.getOption("2nd_score").getAsInt(),
                event.getOption("3rd_score").getAsInt(),
                event.getOption("4th_score").getAsInt()
        };

        long[] userIds = {
                event.getOption("1st_name").getAsUser().getIdLong(),
                event.getOption("2nd_name").getAsUser().getIdLong(),
                event.getOption("3rd_name").getAsUser().getIdLong(),
                event.getOption("4th_name").getAsUser().getIdLong()
        };

        String[] names = {
                event.getOption("1st_name").getAsUser().getEffectiveName(),
                event.getOption("2nd_name").getAsUser().getEffectiveName(),
                event.getOption("3rd_name").getAsUser().getEffectiveName(),
                event.getOption("4th_name").getAsUser().getEffectiveName()
        };

        @GuildRes long guildId = getGuildID(event);
        @UserRes long userId = event.getUser().getIdLong();
        String gameGroup = getGameGroup(event);

        Game game = new Game(guildId, userId, gameGroup);
        List<GameResult> gameResult = IntStream.rangeClosed(0, 3).mapToObj(i ->
                new GameResult(game.getId(), userIds[i], i + 1, scores[i])
        ).toList();

        try {
            int game_id = DBScoreProcess.INSTANCE.addScore(game, gameResult);
            int game_count = DBHandler.INSTANCE.getGameCount(game_id, guildId, gameGroup);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(resourceBundle.getString("add.embed.success.title"));
            for (int i = 0; i < 4; i++) {
                embed.addField(
                        String.format(resourceBundle.getString("add.embed.success.field"), i + 1, names[i]),
                        String.valueOf(scores[i]),
                        true
                );
            }
            embed.setFooter(
                    String.format(
                            resourceBundle.getString("add.embed.success.footer"),
                            game_id,
                            game_count,
                            game.getCreatedAt()
                    )
            );
            embed.setColor(Color.BLACK);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            Logger.addEvent(event);
        }
        catch (ParameterErrorException | GameGroupNotFoundException | DBConnectException e) {
            event.getHook().sendMessageEmbeds(e.getErrorEmbed(event.getUserLocale())).setEphemeral(true).queue();

            if (e instanceof ParameterErrorException)
                Logger.addErrorEvent(event, Logger.PARAM_ERR);
            if (e instanceof GameGroupNotFoundException)
                Logger.addErrorEvent(event, Logger.UNKNOWN_GAMEGROUP);
        }
    }
}