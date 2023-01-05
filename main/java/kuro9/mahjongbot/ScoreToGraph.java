package kuro9.mahjongbot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScoreToGraph extends Frame {
    public static void main(String[] args) {
        ScoreToGraph frame = new ScoreToGraph();
        frame.setSize(831, 251);
        frame.setBackground(Color.BLUE);

        frame.setVisible(true);
        frame.scoreGraphGen();
    }

    public void paintComponent(Graphics g) {
        super.paintComponents(g);
    }

    public ScoreToGraph() {
        setBackground(Color.BLUE);
    }

    public void scoreGraphGen() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        paint(graphics2D);
        try {
            ImageIO.write(image, "png", new File("C:\\javapic.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
