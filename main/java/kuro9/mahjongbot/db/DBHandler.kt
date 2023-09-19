package kuro9.mahjongbot.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.GameGroupNotFoundException
import kuro9.mahjongbot.exception.ParameterErrorException
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
     * @return 현재 guild && game group에서의 국 수
     *
     * @throws ParameterErrorException 4명이 아닐 때, 점수별 정렬되어있지 않을 때, 점수 합이 10만점이 아닐 때
     * @throws GameGroupNotFoundException 등록된 game group가 아닐 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(ParameterErrorException::class, GameGroupNotFoundException::class, DBConnectException::class)
    fun addScore(game: Game, result: Collection<GameResult>): Int {
        if (result.size != 4) throw ParameterErrorException("Size is not 4!")
        if (result.withIndex()
                .all { (index, gameResult) -> gameResult.rank != index + 1 }
        ) throw ParameterErrorException("Not Sorted!")
        if (result.map { it.score }
                .reduce { acc, now -> acc + now } != 100000) throw ParameterErrorException("Score sum is invalid!")

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
                        when (val gameCount: Int = getInt(12)) {
                            -2 -> throw GameGroupNotFoundException()
                            -1 -> throw DBConnectException("Procedure Error!")
                            else -> return gameCount
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }

    }


    /**
     * 새 게임 그룹을 추가합니다.
     * @param guildID 길드의 id
     * @param groupName 알파벳, 숫자, 언더바로 구성된 최대 15글자의 게임그룹명
     * @return 성공 여부
     * @throws ParameterErrorException [guildID]가 형식에 맞지 않을 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(ParameterErrorException::class, DBConnectException::class)
    fun addGameGroup(@GuildRes guildID: Long, groupName: String): Boolean {
        if (!checkGameGroup(groupName)) throw ParameterErrorException("Invalid form of game group name!")

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addGameGroupQuery).use { call ->
                    with(call) {
                        setLong(1, guildID)
                        setString(2, groupName)
                        registerOutParameter(3, Types.INTEGER)

                        executeUpdate()
                        return if (getInt(3) == 0) true else throw DBConnectException("Procedure Error!")
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }
    }

    /**
     * 게임 결과를 조회합니다.
     * @param startDate 조회 날짜 범위 시작
     * @param endDate 조회 날짜 범위 끝
     * @param guildID 조회할 서버 ID
     * @param gameGroup 조회할 게임 그룹
     * @param filterGameCount 필터링할 국 수
     * @return 게임 결과 List
     * @throws DBConnectException DB 처리 에러
     */
    @Throws(DBConnectException::class)
    fun selectGameResult(
        startDate: Timestamp? = null,
        endDate: Timestamp? = null,
        @GuildRes guildID: Long,
        gameGroup: String = "",
        filterGameCount: Int = 0
    ): List<GameResult> {
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
            throw DBConnectException()
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
     * @return 게임 결과 List
     * @throws DBConnectException DB 처리 에러
     */
    @Throws(DBConnectException::class)
    fun selectRecentGameResult(
        @GuildRes guildID: Long,
        @UserRes userID: Long,
        startDate: Timestamp? = null,
        endDate: Timestamp? = null,
        gameGroup: String = "",
    ): List<GameResult> {
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
            throw DBConnectException()
        }

        return gameResultList
    }

    fun checkGameGroup(gameGroup: String): Boolean =
        Regex("^[A-Za-z0-9_]{0,15}$").matchEntire(gameGroup) !== null

}