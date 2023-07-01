package kuro9.mahjongbot.data

import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.IntRange
import kuro9.mahjongbot.annotation.UserRes
import org.apache.commons.math3.util.Precision.round

data class UserGameData(@UserRes val id: Long) {
    /** 연산 편의성 위해 10을 곱해 정수로 저장 */
    private var _totalUma: Long = 0
    val totalUma: Double
        get() = _totalUma / 10.0

    /** 1등, 2등, 3등, 4등, 토비 */
    val rankCount: IntArray = intArrayOf(0, 0, 0, 0, 0)
    val gameCount: Int get() = rankCount.sum() - rankCount[4]

    //TODO 작동하는지 확인필(소수점 반올림)
    /** 1등, 2등, 3등, 4등, 토비 */
    val rankPercentage: Array<Double>
        get() = rankCount.map {
            round((it / gameCount.toDouble() * 100.0), 2)
        }.toTypedArray()

    val avgRank: Double
        get() = round(
            (rankCount[0] + (rankCount[1] * 2) + (rankCount[2] * 3) + (rankCount[3] * 4) / gameCount.toDouble() * 100.0),
            2
        )

    val avgUma: Double
        get() = round(totalUma / gameCount, 2)

    fun addGameData(score: Int, @IntRange(1, 4) rank: Int) {
        _totalUma += (((score - Setting.RETURN_POINT) / 1000.0 + Setting.UMA[rank - 1]) * 10).toLong()
    }
}