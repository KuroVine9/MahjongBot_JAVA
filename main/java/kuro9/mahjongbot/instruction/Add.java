package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.DBScoreProcess;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.annotation.GuildRes;
import kuro9.mahjongbot.annotation.UserRes;
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
public class Add extends GameDataParse {

    public static void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        if (!event.isFromGuild()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("403 Forbidden");
            embed.addField(
                    resourceBundle.getString("exception.not_in_guild.name"),
                    resourceBundle.getString("exception.not_in_guild.description"),
                    true
            );
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).queue();

            Logger.addErrorEvent(event, Logger.NOT_GUILD_MSG);
            return;
        }

        var options = event.getOptions();
        String[] names = new String[4];
        long[] userIds = new long[4];
        int[] scores = new int[4];
        for (int i = 0; i < options.size(); i++) {
            names[i / 2] = options.get(i).getAsUser().getEffectiveName();
            userIds[i / 2] = options.get(i).getAsUser().getIdLong();
            scores[i / 2] = (int) options.get(++i).getAsLong();
        }
        @GuildRes long guildId = getGuildID(event);
        @UserRes long userId = event.getUser().getIdLong();
        String gameGroup = getGameGroup(event);

        Game game = new Game(guildId, userId, gameGroup);
        List<GameResult> gameResult = IntStream.rangeClosed(0, 3).mapToObj(i ->
                new GameResult(game.getId(), userIds[i], i + 1, scores[i])
        ).toList();

        try {
            int game_count = DBScoreProcess.INSTANCE.addScore(game, gameResult);

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
                            game_count,
                            game.getCreatedAt()
                    )
            );
            embed.setColor(Color.BLACK);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            Logger.addEvent(event);
        }
        catch (ParameterErrorException | GameGroupNotFoundException | DBConnectException e) {
            EmbedBuilder embed = e.getErrorEmbed(event.getUserLocale());
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

            if (e instanceof ParameterErrorException)
                Logger.addErrorEvent(event, Logger.PARAM_ERR);
            if (e instanceof GameGroupNotFoundException)
                Logger.addErrorEvent(event, Logger.UNKNOWN_GAMEGROUP);
        }
    }
}