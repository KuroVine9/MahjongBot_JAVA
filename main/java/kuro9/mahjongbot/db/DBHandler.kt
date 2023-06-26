package kuro9.mahjongbot.db

import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types


object DBHandler {

    private var db: Connection? = null
    private lateinit var addScorePS: CallableStatement
    private lateinit var addGameGroupPS: CallableStatement

    private val isConnected: Boolean
        get() = (db !== null) && (db?.isValid(1) ?: false)

    @Volatile
    private var isConnecting: Boolean = false


    init {
        connect()
    }

    private fun connect() {
        if (isConnecting || isConnected) return
        isConnecting = true

        kotlin.runCatching {
            Class.forName("com.mysql.cj.jdbc.Driver")
            db = DriverManager.getConnection(
                Setting.DB_URL,
                Setting.DB_USER,
                Setting.DB_PASSWORD
            )
        }.onFailure {
            println("[DBHandler] Connect Fail.")
        }.onSuccess {
            println("[DBHandler] Connected!")
            isConnecting = false
            db!!.apply {
                addScorePS = prepareCall("CALL add_score(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                addGameGroupPS = prepareCall("CALL add_group(?, ?, ?)")
            }
        }

    }

    /**
     * 점수를 추가합니다.
     * @param game [Game] 객체
     * @param result size가 4인 [GameResult] 객체 배열
     * @return 현재 guild && game group에서의 국 수, SQL INSERT 에러 시 -1, game group가 존재하지 않을 시 -2, DB 연결에러 시 -100, 파라미터 에러 시 -101
     */
    fun addScore(game: Game, result: Array<GameResult>): Int {
        connect()
        if (!isConnected) return -100
        if (result.size != 4) return -101 //파라미터 에러
        if (result.withIndex().all { (index, gameResult) -> gameResult.rank != index + 1 }) return -101
        if (result.map { it.score }.reduce { acc, now -> acc + now } != 100000) return -101
        // statement에 따라 다른 에러코드 return


        addScorePS.apply {
            setLong(1, game.guildID)
            setString(2, game.gameGroup)
            setLong(3, game.addedBy)

            result.forEach {
                setLong(it.rank * 2 + 2, it.userID)
                setInt(it.rank * 2 + 3, it.score)
            }

            registerOutParameter(12, Types.INTEGER)
        }

        addScorePS.executeUpdate()
        val gameCount: Int = addScorePS.getInt(12)
        print(gameCount)
        return gameCount
    }

    /**
     * 새 게임 그룹을 추가합니다.
     * @param guildID 길드의 id
     * @param groupName 알파벳, 숫자, 언더바로 구성된 최대 15글자의 게임그룹명
     * @return 성공여부. 성공 시 0, 실패 시 -1, DB 연결에러 시 -100, 파라미터 에러 시 -101
     */
    fun addGameGroup(@GuildRes guildID: Long, groupName: String): Int {
        connect()
        if (!isConnected) return -100
        if (Regex("^[A-Za-z0-9_]{1,15}$").matchEntire(groupName) === null) return -101

        addGameGroupPS.apply {
            setLong(1, guildID)
            setString(2, groupName)
            registerOutParameter(3, Types.INTEGER)
        }
        addGameGroupPS.executeUpdate()
        return addGameGroupPS.getInt(3)
    }
}