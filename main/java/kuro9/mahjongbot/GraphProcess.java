package kuro9.mahjongbot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 그래프 이미지를 생성하기 위한 클래스입니다.
 */
public class GraphProcess extends JFrame {
    /**
     * 그림이 그려질 패널을 호출 후 저장합니다.
     *
     * @param recent_data [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     */
    public void scoreGraphGen(int[][] recent_data) {
        var pn = new MyPanel(recent_data);
        setContentPane(pn);
        setSize(847, 290);
        setVisible(true);
        pn.scoreGraphGen();
        dispose();
    }

    class MyPanel extends JPanel {
        final int[] x_axis = {37, 119, 202, 284, 367, 449, 532, 614, 697, 779};
        final int[] y_axis = {195, 149, 102, 56};

        int[][] recent_data;

        MyPanel(int[][] recent_data) {
            this.recent_data = recent_data;
        }

        /**
         * 패널 위에 그래프를 그립니다.
         *
         * @param g the <code>Graphics</code> object to protect
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon background = new ImageIcon(Setting.IMAGE_BACKGROUND_PATH);
            ImageIcon nyan_glass = new ImageIcon(Setting.IMAGE_NYANGLASS_PATH);
            g.drawImage(background.getImage(), 0, 0, null);
            Graphics2D g2 = (Graphics2D) g;

            g.setColor(new Color(0xD6, 0x5F, 0x2A)); // d65f2a
            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_BUTT, 0));
            for (int i = 0; i < 9; i++) {// 1->3    2->2    3->1    4->0
                if (recent_data[0][i] == 0) break;
                else if (recent_data[0][i + 1] != 0)
                    g2.draw(new Line2D.Double(
                            x_axis[i] + 26, y_axis[4 - recent_data[0][i]],
                            x_axis[i + 1] + 26, y_axis[4 - recent_data[0][i + 1]]
                    ));
                else break;
            }
            g.setColor(new Color(0xFF, 0xC0, 0x29)); // ffc029
            for (int i = 0; i < 10; i++) {
                if (recent_data[0][i] != 0) {
                    g2.fillOval(x_axis[i] + 18, y_axis[4 - recent_data[0][i]] - 7, 15, 15);
                    if (recent_data[1][i] == 1)
                        g.drawImage(
                                nyan_glass.getImage(),
                                x_axis[i] - 14,
                                y_axis[4 - recent_data[0][i]] - 30,
                                null
                        );
                }
                else break;
            }
        }

        /**
         * 그려진 그래프를 저장합니다.
         */
        public void scoreGraphGen() {
            BufferedImage image = new BufferedImage(831, 251, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            paint(graphics2D);
            try {
                ImageIO.write(image, "png", new File(Setting.GRAPH_PATH));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}