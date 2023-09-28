package kuro9.mahjongbot.instruction;

import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.MachiCalculator;
import kuro9.mahjongbot.ResourceHandler;
import kuro9.mahjongbot.Setting;
import kuro9.mahjongbot.data.PaiDiscardMachiData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class MahjongCalc {
    public static void getAllMachi(SlashCommandInteractionEvent event) {
        ResourceBundle resourceBundle = ResourceHandler.getResource(event);
        String hand = event.getOption("hand").getAsString();

        // 파라미터 검사
        if (
                (!Pattern.matches("^([1-9]+[mspz]?)+$", hand))
                        || (hand.indexOf('m') != hand.lastIndexOf('m'))
                        || (hand.indexOf('s') != hand.lastIndexOf('s'))
                        || (hand.indexOf('p') != hand.lastIndexOf('p'))
                        || (hand.indexOf('z') != hand.lastIndexOf('z'))
                        || (hand.indexOf('m') == -1 && hand.indexOf('s') == -1 &&
                        hand.indexOf('p') == -1 && hand.indexOf('z') == -1)
        ) {
            event.deferReply(true).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("400 Bad Request");
            embed.setDescription("Parameter Err.");
            embed.addField(
                    resourceBundle.getString("mahjongcalc.embed.err.400.name"),
                    resourceBundle.getString("mahjongcalc.embed.err.400.description"),
                    true
            );
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            Logger.addErrorEvent(event, Logger.PARAM_ERR);
            return;
        }
        ArrayList<Integer> hand_list = MachiCalculator.getIntList(hand);

        // 이미지 생성 실패
        if (getHandPicture(hand) == -1) {
            event.deferReply(true).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("503 Service Unavailable");
            embed.setDescription("Image Generate Err.");
            embed.addField(
                    resourceBundle.getString("mahjongcalc.embed.err.503.name"),
                    resourceBundle.getString("mahjongcalc.embed.err.503.description"),
                    true
            );
            embed.setColor(Color.RED);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            Logger.addErrorEvent(event, Logger.HAND_IMG_GEN_ERR);
            return;
        }
        event.deferReply().queue();
        if (hand_list.size() % 3 == 1) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(hand);
            embed.setDescription(String.format(
                    resourceBundle.getString("mahjongcalc.embed.machi.description"),
                    MachiCalculator.getString(MachiCalculator.getMachi(hand))
            ));
            event.getHook().sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(new File(Setting.MAHJONG_BASE_PATH + "hand.png"))).queue();
        }
        else if (hand_list.size() % 3 == 2) {
            LinkedList<PaiDiscardMachiData> list = (LinkedList<PaiDiscardMachiData>) MachiCalculator.getTenpaiMachi(hand);
            list.sort((dataA, dataB) -> dataB.nokoru_pai - dataA.nokoru_pai);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(hand);
            for (var li : list) {
                embed.addField(
                        String.format(resourceBundle.getString("mahjongcalc.embed.allmachi.field.name"), li.pai),
                        String.format(resourceBundle.getString("mahjongcalc.embed.allmachi.field.description"), li.nokoru_pai, MachiCalculator.getString(li.machi)),
                        false
                );
            }
            event.getHook().sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(new File(Setting.MAHJONG_BASE_PATH + "hand.png"))).queue();
        }
        Logger.addEvent(event);
    }

    public static void getScore(SlashCommandInteractionEvent event) {
        //TODO 부수판수 계산기
    }

    public static int getHandPicture(String hand) {
        ArrayList<Integer> hand_int = MachiCalculator.getIntList(hand);
        try {
            LinkedList<BufferedImage> images = new LinkedList<>();
            for (Integer pai : hand_int)
                images.add(ImageIO.read(new File(getPaiPath(pai))));
            int width = images.stream().mapToInt(BufferedImage::getWidth).sum();
            int height = 100;

            BufferedImage merge = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphic = (Graphics2D) merge.getGraphics();

            // graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
            graphic.setColor(new Color(0x36, 0x39, 0x3F));
            graphic.fillRect(0, 0, width, height);

            int width_sum = 0;
            for (var image : images) {
                graphic.drawImage(image, width_sum, 0, null);
                width_sum += image.getWidth();
            }

            graphic.dispose();

            ImageIO.write(merge, "png", new File(Setting.MAHJONG_BASE_PATH + "hand.png"));
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    private static String getPaiPath(int pai) {
        return switch (pai / 10) {
            case 0 -> String.format("%spai/manzu/manzu%d.png", Setting.MAHJONG_BASE_PATH, pai % 10);
            case 1 -> String.format("%spai/pinzu/pinzu%d.png", Setting.MAHJONG_BASE_PATH, pai % 10);
            case 2 -> String.format("%spai/souzu/souzu%d.png", Setting.MAHJONG_BASE_PATH, pai % 10);
            default -> String.format("%spai/zihai/zihai%d.png", Setting.MAHJONG_BASE_PATH, pai / 10 - 2);
        };
    }

    private static String getPaiPath(char iro, int num) {
        return switch (iro) {
            case 'm' -> String.format("%spai/manzu/manzu%d.png", Setting.MAHJONG_BASE_PATH, num);
            case 'p' -> String.format("%spai/pinzu/pinzu%d.png", Setting.MAHJONG_BASE_PATH, num);
            case 's' -> String.format("%spai/souzu/souzu%d.png", Setting.MAHJONG_BASE_PATH, num);
            case 'z' -> String.format("%spai/zihai/zihai%d.png", Setting.MAHJONG_BASE_PATH, num);
            default -> throw new IllegalArgumentException("No Valid Parameter!");
        };
    }
}
