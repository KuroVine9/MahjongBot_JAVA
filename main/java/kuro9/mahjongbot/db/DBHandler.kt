package kuro9.mahjongbot.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types


object DBHandler {
    private var dataSource: HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = Setting.DB_URL
            username = Setting.DB_USER
            password = Setting.DB_PASSWORD
            maximumPoolSize = 8
        }
    )

    private const val addScoreQuery = "CALL add_score(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    private const val addGameGroupQuery = "CALL add_group(?, ?, ?)"
    private const val selectGameResultQuery = "CALL select_record(?, ?, ?, ?, ?)"
    private const val selectRecentGameResultQuery = "CALL recent_ten_record(?, ?, ?, ?, ?)"


    /**
     * 점수를 추가합니다.
     * @param game [Game] 객체
     * @param result size가 4인 [GameResult] 객체 배열
     * @return 현재 guild && game group에서의 국 수, SQL INSERT 에러 시 -1, game group가 존재하지 않을 시 -2, DB 연결에러 시 -100, 파라미터 에러 시 <= -101
     * (4명이 아닐 시 -101, 점수별 정렬되어있지 않을 시 -102, 점수 합이 10만점이 아닐 시 -103)
     */
    fun addScore(game: Game, result: Collection<GameResult>): Int {
        if (result.size != 4) return -101 //파라미터 에러
        if (result.withIndex().all { (index, gameResult) -> gameResult.rank != index + 1 }) return -102
        if (result.map { it.score }.reduce { acc, now -> acc + now } != 100000) return -103
        // statement에 따라 다른 에러코드 return

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addScoreQuery).use { call ->
                    with(call) {
                        setLong(1, game.guildID)
                        setString(2, game.gameGroup)
                        setLong(3, game.addedBy)

                        result.forEach {
                            setLong(it.rank * 2 + 2, it.userID)
                            setInt(it.rank * 2 + 3, it.score)
                        }

                        registerOutParameter(12, Types.INTEGER)

                        executeUpdate()
                        return getInt(12)
                    }
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
            return -100
        }
    }


    /**
     * 새 게임 그룹을 추가합니다.
     * @param guildID 길드의 id
     * @param groupName 알파벳, 숫자, 언더바로 구성된 최대 15글자의 게임그룹명
     * @return 성공여부. 성공 시 0, 실패 시 -1, DB 연결에러 시 -100, 파라미터 에러 시 -101
     */
    fun addGameGroup(@GuildRes guildID: Long, groupName: String): Int {
        if (!checkGameGroup(groupName)) return -101

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addGameGroupQuery).use { call ->
                    with(call) {
                        setLong(1, guildID)
                        setString(2, groupName)
                        registerOutParameter(3, Types.INTEGER)

                        executeUpdate()
                        return getInt(3)
                    }
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
            return -100
        }
    }

    /**
     * 게임 결과를 조회합니다.
     * @param startDate 조회 날짜 범위 시작
     * @param endDate 조회 날짜 범위 끝
     * @param guildID 조회할 서버 ID
     * @param gameGroup 조회할 게임 그룹
     * @param filterGameCount 필터링할 국 수
     * @return 게임 결과 List. db connection error시 null
     */
    fun selectGameResult(
        startDate: Timestamp? = null,
        endDate: Timestamp? = null,
        @GuildRes guildID: Long,
        gameGroup: String = "",
        filterGameCount: Int = 0
    ): List<GameResult>? {
        val gameResultList = mutableListOf<GameResult>()

        // 파라미터 체크
        if (!checkGameGroup(gameGroup)) return gameResultList

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(selectGameResultQuery).use { call ->
                    with(call) {
                        setTimestamp(1, startDate ?: Timestamp.valueOf("2002-10-24 00:00:00"))
                        setTimestamp(2, endDate ?: Timestamp(System.currentTimeMillis()))
                        setLong(3, guildID)
                        setString(4, gameGroup)
                        setInt(5, filterGameCount)

                        executeQuery().use { resultSet ->
                            with(resultSet) {
                                while (next())
                                    gameResultList.add(
                                        GameResult(
                                            gameID = getInt(1),
                                            userID = getLong(2),
                                            rank = getInt(3),
                                            score = getInt(4)
                                        )
                                    )
                            }
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
            return null
        }

        return gameResultList
    }

    /**
     * 게임 결과를 조회합니다.
     * @param guildID 조회할 서버 ID
     * @param userID 조회할 유저 ID
     * @param startDate 조회 날짜 범위 시작
     * @param endDate 조회 날짜 범위 끝
     * @param gameGroup 조회할 게임 그룹
     * @return 게임 결과 List. db connection error시 null
     */
    fun selectRecentGameResult(
        @GuildRes guildID: Long,
        @UserRes userID: Long,
        startDate: Timestamp? = null,
        endDate: Timestamp? = null,
        gameGroup: String = "",
    ): List<GameResult>? {
        val gameResultList = mutableListOf<GameResult>()

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(selectRecentGameResultQuery).use { call ->
                    with(call) {
                        setTimestamp(1, startDate ?: Timestamp.valueOf("2002-10-24 00:00:00"))
                        setTimestamp(2, endDate ?: Timestamp(System.currentTimeMillis()))
                        setLong(3, guildID)
                        setLong(4, userID)
                        setString(5, gameGroup)

                        executeQuery().use { resultSet ->
                            with(resultSet) {
                                while (next())
                                    gameResultList.add(
                                        GameResult(
                                            gameID = getInt("game_id"),
                                            userID = getLong("user_id"),
                                            rank = getInt("rank"),
                                            score = getInt("score")
                                        )
                                    )
                            }
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
            return null
        }

        return gameResultList
    }

    private fun checkGameGroup(gameGroup: String): Boolean =
        Regex("^[A-Za-z0-9_]{0,15}$").matchEntire(gameGroup) !== null

}