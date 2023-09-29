package kuro9.mahjongbot.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.UserRes
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameRecord
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.*
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.sql.Timestamp
import java.sql.Types.*


object DBHandler {
    private var dataSource: HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = Setting.DB_URL
            username = Setting.DB_USER
            password = Setting.DB_PASSWORD
            maximumPoolSize = 8
        }
    )

    private const val addScoreQuery = "CALL add_score(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)"
    private const val addGameGroupQuery = "CALL add_group(?, ?, ?)"
    private const val selectGameResultQuery = "CALL select_record(?, ?, ?, ?, ?)"
    private const val selectRecentGameResultQuery = "CALL recent_ten_record(?, ?, ?, ?, ?)"
    private const val selectGameGroupQuery = "CALL select_gamegroup(?)"
    private const val modifyRecordQuery = "CALL modify_record(?, ?,?,?,?,?,?,?,?,?,?,?)"
    private const val deleteRecordQuery = "CALL delete_record(?,?,?,?)"
    private const val addAdminQuery = "CALL add_admin(?,?)"
    private const val selectAdminQuery = "CALL select_admin(?)"
    private const val deleteAdminQuery = "CALL delete_admin(?,?)"
    private const val getGameCountQuery = "CALL get_game_count(?, ?, ?, ?)"
    private const val getGameDataQuery = "CALL get_game_data(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?)"
    private const val addTempScoreQuery = "CALL add_temp_score(?,?,?,?,?,?,?,?,?,?,?,?)"
    private const val getTempScoreQuery = "CALL get_temp_score(?,?,?,?)"
    private const val registerNameQuery = "CALL register_name(?,?)"


    /**
     * 점수를 추가합니다.
     * @param game [Game] 객체
     * @param result size가 4인 [GameResult] 객체 배열
     * @return 게임ID
     *
     * @throws AddParameterErrorException 4명이 아닐 때, 점수별 정렬되어있지 않을 때, 점수 합이 10만점이 아닐 때
     * @throws InvalidGameGroupPatternException 유효한 게임 그룹이 아닐 때
     * @throws GameGroupNotFoundException 등록된 game group가 아닐 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(
        AddParameterErrorException::class,
        InvalidGameGroupPatternException::class,
        GameGroupNotFoundException::class,
        DBConnectException::class
    )
    fun addScore(game: Game, result: Collection<GameResult>): Int {
        if (!checkGameGroup(game.gameGroup))
            throw InvalidGameGroupPatternException()
        checkGameResult(result)

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addScoreQuery).use { call ->
                    with(call) {
                        setLong(1, game.guildID)
                        setString(2, game.gameGroup)
                        setLong(3, game.addedBy)

                        result.forEach {
                            setLong(it.rank * 3 + 1, it.userID)
                            setString(it.rank * 3 + 2, it.name)
                            setInt(it.rank * 3 + 3, it.score)
                        }

                        registerOutParameter(16, INTEGER)

                        executeUpdate()
                        when (val gameId: Int = getInt(16)) {
                            -400 -> throw GameGroupNotFoundException()
                            -1 -> throw DBConnectException("Procedure Error!")
                            else -> return gameId
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
     * 등록한 게임이 몇 번째 게임인지 리턴합니다.
     *
     * @param game id, guildId, gameGroup가 valid해야 함.
     * @return 속한 서버의 게임 그룹에서의 게임 카운트
     * @throws AddParameterErrorException 게임 그룹 패턴 체크
     * @throws DBConnectException DB 처리 에러
     */
    @Throws(AddParameterErrorException::class, DBConnectException::class)
    fun getGameCount(game: Game): Int {
        if (!checkGameGroup(game.gameGroup))
            throw AddParameterErrorException("Invalid GameGroup Pattern!")

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(getGameCountQuery).use { call ->
                    with(call) {
                        setInt(1, game.id)
                        setLong(2, game.guildID)
                        setString(3, game.gameGroup)

                        registerOutParameter(4, INTEGER)

                        executeUpdate()

                        return getInt(4)
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }
    }

    /**
     * 등록한 게임이 몇 번째 게임인지 리턴합니다.
     *
     * @param gameId 게임의 ID
     * @param guildId 서버 ID
     * @param gameGroup 게임 그룹
     *
     * @return 속한 서버의 게임 그룹에서의 게임 카운트
     * @throws AddParameterErrorException 게임 그룹 패턴 체크
     * @throws DBConnectException DB 처리 에러
     */
    @Throws(AddParameterErrorException::class, DBConnectException::class)
    fun getGameCount(gameId: Int, @GuildRes guildId: Long, gameGroup: String): Int {
        return getGameCount(Game(guildId, -1, gameGroup).apply { id = gameId })
    }

    /**
     * 특정 게임 기록을 불러옵니다.
     * @param gameId 불러올 게임ID
     * @return [GameRecord]형의 게임 기록
     * @throws DBConnectException DB 처리 에러
     * @throws GameNotFoundException 게임을 찾을 수 없는 경우
     */
    @Throws(DBConnectException::class, GameNotFoundException::class)
    fun getGameData(gameId: Int): GameRecord {
        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(getGameDataQuery).use { call ->
                    with(call) {
                        setInt(1, gameId)
                        registerOutParameter("result", INTEGER)
                        registerOutParameter("guildID", BIGINT)
                        registerOutParameter("createdAt", TIMESTAMP)
                        registerOutParameter("gameGroup", VARCHAR)
                        registerOutParameter("addedBy", BIGINT)

                        registerOutParameter("firstId", BIGINT)
                        registerOutParameter("firstName", VARCHAR)
                        registerOutParameter("firstScore", INTEGER)

                        registerOutParameter("secondId", BIGINT)
                        registerOutParameter("secondName", VARCHAR)
                        registerOutParameter("secondScore", INTEGER)

                        registerOutParameter("thirdId", BIGINT)
                        registerOutParameter("thirdName", VARCHAR)
                        registerOutParameter("thirdScore", INTEGER)

                        registerOutParameter("fourthId", BIGINT)
                        registerOutParameter("fourthName", VARCHAR)
                        registerOutParameter("fourthScore", INTEGER)

                        executeUpdate()

                        return when (getInt("result")) {
                            -1 -> throw DBConnectException("Procedure Error!")
                            -404 -> throw GameNotFoundException()
                            else -> {
                                val game = Game(getLong("guildID"), getLong("addedBy"), getString("gameGroup")).apply {
                                    id = gameId
                                    createdAt = getTimestamp("createdAt")
                                }
                                val gameResults = arrayOf(
                                    GameResult(
                                        gameId,
                                        getLong("firstId"),
                                        1,
                                        getInt("firstScore"),
                                        getString("firstName")
                                    ),
                                    GameResult(
                                        gameId,
                                        getLong("secondId"),
                                        2,
                                        getInt("secondScore"),
                                        getString("secondName")
                                    ),
                                    GameResult(
                                        gameId,
                                        getLong("thirdId"),
                                        3,
                                        getInt("thirdScore"),
                                        getString("thirdName")
                                    ),
                                    GameResult(
                                        gameId,
                                        getLong("fourthId"),
                                        4,
                                        getInt("fourthScore"),
                                        getString("fourthName")
                                    ),
                                )

                                GameRecord(game, gameResults)
                            }
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
     * @throws AddParameterErrorException [guildID]가 형식에 맞지 않을 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(AddParameterErrorException::class, DBConnectException::class)
    fun addGameGroup(@GuildRes guildID: Long, groupName: String) {
        if (!checkGameGroup(groupName)) throw AddParameterErrorException("Invalid form of game group name!")

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addGameGroupQuery).use { call ->
                    with(call) {
                        setLong(1, guildID)
                        setString(2, groupName)
                        registerOutParameter(3, INTEGER)

                        executeUpdate()

                        when (getInt(3)) {
                            -1 -> throw DBConnectException("Procedure Error!")
                            -400 -> throw AddParameterErrorException("Not a Valid GameGroup!")
                            else -> {
                                // DO NOTHING
                            }
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
                                            score = getInt(4),
                                            name = getString(5)
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
                                            score = getInt("score"),
                                            name = null
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
     * 게임 그룹를 조회합니다.
     * @param guildID 조회할 서버 ID
     * @return 게임 그룹 List
     * @throws DBConnectException DB 처리 에러
     */
    @Throws(DBConnectException::class)
    fun selectGameGroup(@GuildRes guildID: Long): List<String> {
        val gameGroupList = mutableListOf<String>()

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(selectGameGroupQuery).use { call ->
                    with(call) {
                        setLong(1, guildID)

                        executeQuery().use { resultSet ->
                            with(resultSet) {
                                while (next())
                                    gameGroupList.add(getString(1))
                            }
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }

        return gameGroupList
    }

    /**
     * 등록된 게임을 수정합니다.
     *
     * @param userId 명령어를 실행하는 유저의 ID
     * @param gameId 수정할 게임의 ID
     * @param guildId 수정할 게임이 속한 서버의 ID
     * @param result size가 4인 [GameResult] 객체 배열 - id 필드가 반드시 valid한 값이어야 함
     *
     * @throws AddParameterErrorException 4명이 아닐 때, 점수별 정렬되어있지 않을 때, 점수 합이 10만점이 아닐 때
     * @throws PermissionExpiredException 10분이 지나 더 이상 점수의 수정/삭제가 불가능할 때
     * @throws PermissionDeniedException 점수를 수정/삭제 할 권한이 없을 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     * @throws GameNotFoundException 수정할 게임을 찾을 수 없을 때
     */
    @Throws(
        AddParameterErrorException::class,
        PermissionExpiredException::class,
        DBConnectException::class,
        PermissionDeniedException::class,
        GameNotFoundException::class
    )
    fun modifyRecord(@UserRes userId: Long, gameId: Int, @GuildRes guildId: Long, result: Collection<GameResult>) {
        checkGameResult(result)

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(modifyRecordQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setInt(2, gameId)
                        setLong(3, guildId)

                        result.forEach {
                            setLong(it.rank * 2 + 2, it.userID)
                            setInt(it.rank * 2 + 3, it.score)
                        }

                        registerOutParameter(12, INTEGER)

                        executeUpdate()
                        when (getInt(12)) {
                            0 -> {}
                            -1 -> throw DBConnectException("Procedure Error!")
                            -400 -> throw PermissionExpiredException()
                            -403 -> throw PermissionDeniedException()
                            -404 -> throw GameNotFoundException()
                            else -> throw IllegalStateException("This state cannot be handle")
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
     * 등록된 게임을 삭제합니다.
     *
     * @param userId 명령어를 실행하는 유저의 ID
     * @param gameId 삭제할 게임의 ID
     * @param guildId 삭제할 게임이 등록되어 있는 서버의 ID
     *
     * @throws PermissionExpiredException 10분이 지나 더 이상 점수의 수정/삭제가 불가능할 때
     * @throws PermissionDeniedException 점수를 수정/삭제 할 권한이 없을 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     * @throws GameNotFoundException 삭제하려는 게임을 찾을 수 없을 때
     */
    @Throws(
        PermissionExpiredException::class,
        DBConnectException::class,
        PermissionDeniedException::class,
        GameNotFoundException::class
    )
    fun deleteRecord(@UserRes userId: Long, gameId: Int, @GuildRes guildId: Long) {

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(deleteRecordQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setInt(2, gameId)
                        setLong(3, guildId)

                        registerOutParameter(4, INTEGER)

                        executeUpdate()
                        when (getInt(4)) {
                            0 -> {} // 정상처리
                            -1 -> throw DBConnectException("Procedure Error!")
                            -400 -> throw PermissionExpiredException()
                            -403 -> throw PermissionDeniedException()
                            -404 -> throw GameNotFoundException()
                            else -> throw IllegalStateException() // 이 브랜치에 도달해서는 안 됨.
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
     * 관리자를 등록합니다.
     *
     * @param userId 유저의 ID
     * @param guildId 서버의 ID
     *
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(DBConnectException::class)
    fun addAdmin(@UserRes userId: Long, @GuildRes guildId: Long) {

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addAdminQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setLong(2, guildId)

                        executeUpdate()
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }

    }

    /**
     * 현재 서버의 관리자 리스트를 출력합니다.
     *
     * @param guildId 서버의 ID
     *
     * @return 관리자의 ID 목록
     *
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(DBConnectException::class)
    fun selectAdmin(@GuildRes guildId: Long): List<Long> {
        val adminList = mutableListOf<Long>()

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(selectAdminQuery).use { call ->
                    with(call) {
                        setLong(1, guildId)

                        executeQuery().use { resultSet ->
                            with(resultSet) {
                                while (next())
                                    adminList.add(getLong(1))
                            }
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }

        return adminList
    }

    /**
     * 관리자를 삭제합니다.
     *
     * @param userId 유저의 ID
     * @param guildId 서버의 ID
     *
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(DBConnectException::class)
    fun deleteAdmin(@UserRes userId: Long, @GuildRes guildId: Long) {

        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(deleteAdminQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setLong(2, guildId)

                        executeUpdate()
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }

    }

    @Throws(
        AddParameterErrorException::class,
        DBConnectException::class,
        PermissionDeniedException::class,
        PermissionExpiredException::class,
        GameNotFoundException::class,
        DataConflictException::class
    )
    fun addTempScore(@UserRes userId: Long, @GuildRes guildId: Long, gameId: Int, result: Collection<GameResult>) {
        checkGameResult(result)
        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(addTempScoreQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setInt(2, gameId)
                        setLong(3, guildId)

                        result.forEach {
                            setLong(it.rank * 2 + 2, it.userID)
                            setInt(it.rank * 2 + 3, it.score)
                        }

                        registerOutParameter(12, INTEGER)

                        executeUpdate()

                        when (getInt(12)) {
                            0 -> {} // 정상처리
                            -1 -> throw DBConnectException("Procedure Error!")
                            -400 -> throw PermissionExpiredException()
                            -403 -> throw PermissionDeniedException()
                            -404 -> throw GameNotFoundException()
                            -409 -> throw DataConflictException()
                            else -> throw IllegalStateException() // 이 브랜치에 도달해서는 안 됨.
                        }
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }
    }

    @Throws(DBConnectException::class, GameNotFoundException::class)
    fun getTempScore(@UserRes userId: Long, @GuildRes guildId: Long, gameId: Int): List<GameResult> {
        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(getTempScoreQuery).use { call ->
                    with(call) {
                        setLong(1, userId)
                        setInt(2, gameId)
                        setLong(3, guildId)


                        registerOutParameter(4, INTEGER)

                        executeQuery().use { resultSet ->

                            when (getInt("result")) {
                                0 -> {} // 정상처리
                                -1 -> throw DBConnectException("Procedure Error!")
                                -404 -> throw GameNotFoundException()
                                else -> throw IllegalStateException() // 이 브랜치에 도달해서는 안 됨.
                            }

                            with(resultSet) {
                                if (next())
                                    return listOf<GameResult>(
                                        GameResult(
                                            userID = getLong("first_id"),
                                            score = getInt("first_score"),
                                            rank = 1,
                                            name = null
                                        ),
                                        GameResult(
                                            userID = getLong("second_id"),
                                            score = getInt("second_score"),
                                            rank = 2,
                                            name = null
                                        ),
                                        GameResult(
                                            userID = getLong("third_id"),
                                            score = getInt("third_score"),
                                            rank = 3,
                                            name = null
                                        ),
                                        GameResult(
                                            userID = getLong("fourth_id"),
                                            score = getInt("fourth_score"),
                                            rank = 4,
                                            name = null
                                        ),
                                    )
                                else throw IllegalStateException()
                            }
                        }


                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException()
        }
        catch (e: SQLTimeoutException) {
            throw DBConnectException("SQL Timeout!")
        }
    }

    @Throws(DBConnectException::class)
    fun registerName(@UserRes id: Long, name: String) {
        try {
            dataSource.connection.use { connection ->
                connection.prepareCall(registerNameQuery).use { call ->
                    with(call) {
                        setLong(1, id)
                        setString(2, name)

                        executeUpdate()
                    }
                }
            }
        }
        catch (e: SQLException) {
            throw DBConnectException("Procedure E")
        }
    }

    @Throws(AddParameterErrorException::class)
    private fun checkGameResult(result: Collection<GameResult>) {
        if (result.size != 4) throw AddParameterErrorException("Size is not 4!")
        if (result.withIndex()
                .all { (index, gameResult) -> gameResult.rank != index + 1 }
        ) throw AddParameterErrorException("Not Sorted!")
        if (result.map { it.score }
                .reduce { acc, now -> acc + now } != 100000) throw AddParameterErrorException("Score sum is invalid!")
        if (result.map { it.userID }.distinct().size != 4) throw AddParameterErrorException("Duplicated ID!")
    }

    fun checkGameGroup(gameGroup: String): Boolean =
        Regex("^[A-Za-z0-9_]{0,15}$").matchEntire(gameGroup) !== null

}