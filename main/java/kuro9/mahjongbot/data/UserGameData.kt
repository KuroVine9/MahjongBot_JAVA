package kuro9.mahjongbot.data

import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.IntRange
import kuro9.mahjongbot.annotation.UserRes
import org.apache.commons.math3.util.Precision.round

data class UserGameData(@UserRes val id: Long) {

    /** 연산 캐싱을 위한 파라미터 */
    private var _isValid: Boolean = true
    private var _isProcessing: Boolean = false

    var _userName: String? = null
    val userName: String
        get() {
            if (_userName == null)
                _userName = Setting.JDA.retrieveUserById(id).complete().effectiveName
            return _userName!!
        }

    /** 연산 편의성 위해 10을 곱해 정수로 저장 */
    var totalUmaLong: Long = 0

    /** 실제 총 우마 값(소수 첫째 자리) */
    val totalUma: Double
        get() = totalUmaLong / 10.0

    /** 1등, 2등, 3등, 4등, 토비 */
    val rankCount: IntArray = intArrayOf(0, 0, 0, 0, 0)


    /* PRIVATE VALUE BEGIN */
    private var _gameCount: Int = 0
    private var _rankPercentage: DoubleArray = doubleArrayOf(.0, .0, .0, .0, .0)
    private var _avgRank: Double = .0
    private var _avgUma: Double = .0
    /* PRIVATE VALUE END */


    /* COMPUTED GETTER VALUE BEGIN */
    val gameCount: Int
        get() {
            checkData()
            return _gameCount
        }

    //TODO 작동하는지 확인필(소수점 반올림)
    /** 1등, 2등, 3등, 4등, 토비 */
    val rankPercentage: DoubleArray
        get() {
            checkData()
            return _rankPercentage
        }

    val avgRank: Double
        get() {
            checkData()
            return _avgRank
        }

    val avgUma: Double
        get() {
            checkData()
            return _avgUma
        }
    /* COMPUTED GETTER VALUE END */


    fun addGameData(score: Int, @IntRange(1, 4) rank: Int) {
        totalUmaLong += (((score - Setting.RETURN_POINT) / 1000.0 + Setting.UMA[rank - 1]) * 10).toLong()
        if (score < 0) rankCount[4]++
        rankCount[rank - 1]++
        _isValid = false;
    }


    private fun checkData() {
        if (!_isValid && !_isProcessing) {
            _isProcessing = true
            _gameCount = rankCount.sum() - rankCount[4]
            _rankPercentage = rankCount.map {
                round((it / gameCount.toDouble() * 100.0), 2)
            }.toDoubleArray()
            _avgRank = round(
                ((rankCount[0] + (rankCount[1] * 2) + (rankCount[2] * 3) + (rankCount[3] * 4)) / (gameCount.toDouble())),
                2
            )
            _avgUma = round(totalUma / gameCount, 2)

            _isValid = true
            _isProcessing = false
        }
    }
}