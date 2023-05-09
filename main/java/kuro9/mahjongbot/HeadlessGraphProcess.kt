package kuro9.mahjongbot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * 그래프 이미지를 생성하기 위한 클래스입니다.
 */
class HeadlessGraphProcess() {
    val x_axis = intArrayOf(37, 119, 202, 284, 367, 449, 532, 614, 697, 779)
    val y_axis = intArrayOf(195, 149, 102, 56)

    /**
     * 패널 위에 그래프를 그립니다.
     *
     * @param recent_data [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     * @return 생성 성공 여부
     */
    fun scoreGraphGen(recent_data: Array<IntArray>): Boolean {
        val image = BufferedImage(831, 251, BufferedImage.TYPE_INT_RGB)
        val g2 = image.createGraphics()

        lateinit var nyan_glass: BufferedImage;
        lateinit var background: BufferedImage;
        try {
            nyan_glass =
                ImageIO.read(File(Setting.IMAGE_NYANGLASS_PATH))
            background =
                ImageIO.read(File(Setting.IMAGE_BACKGROUND_PATH))
        } catch (e: IOException) {
            Logger.addSystemErrorEvent("image-generate-err");
            e.printStackTrace();
            return false;
        }

        g2.drawImage(background, 0, 0, null)
        g2.color = Color(0xD6, 0x5F, 0x2A) // d65f2a
        g2.stroke = BasicStroke(6f, BasicStroke.CAP_BUTT, 0)
        for (i in 0..8) { // 1->3    2->2    3->1    4->0
            if (recent_data[0][i] == 0) break else if (recent_data[0][i + 1] != 0) g2.draw(
                Line2D.Double(
                    (
                            x_axis[i] + 26).toDouble(), y_axis[4 - recent_data[0][i]].toDouble(),
                    (
                            x_axis[i + 1] + 26).toDouble(), y_axis[4 - recent_data[0][i + 1]]
                        .toDouble()
                )
            ) else break
        }
        g2.color = Color(0xFF, 0xC0, 0x29) // ffc029
        for (i in 0..9) {
            if (recent_data[0][i] != 0) {
                g2.fillOval(x_axis[i] + 18, y_axis[4 - recent_data[0][i]] - 7, 15, 15)
                if (recent_data[1][i] == 1) g2.drawImage(
                    nyan_glass,
                    x_axis[i] - 14,
                    y_axis[4 - recent_data[0][i]] - 30,
                    null
                )
            } else break
        }

        try {
            val outputFile = File(Setting.GRAPH_PATH)
            ImageIO.write(image, "png", outputFile)
        } catch (e: IOException) {
            Logger.addSystemErrorEvent("image-generate-err");
            e.printStackTrace()
            return false;
        }

        return true;
    }
}