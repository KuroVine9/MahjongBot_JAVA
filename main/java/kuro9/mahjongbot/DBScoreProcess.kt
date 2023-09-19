package kuro9.mahjongbot

import kuro9.mahjongbot.annotation.GuildRes
import kuro9.mahjongbot.annotation.IntRange
import kuro9.mahjongbot.annotation.UserRes
import kuro9.mahjongbot.data.UserGameData
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import kuro9.mahjongbot.exception.DBConnectException
import kuro9.mahjongbot.exception.GameGroupNotFoundException
import kuro9.mahjongbot.exception.ParameterErrorException
import java.sql.Timestamp
import java.util.*

object DBScoreProcess {

    data class TimePeriod(val startDate: Timestamp, val endDate: Timestamp)

    /**
     * 데이터 캐싱을 위한 싱글톤 클래스입니다.
     * second-chance를 적용하였습니다.
     */
    private object DataCache {
        private const val QUEUE_SIZE = 8

        private enum class STATE { REFFED, OLD, INVALID }
        data class Query(
            @GuildRes val id: Long,
            val startDate: Timestamp? = null,
            val endDate: Timestamp? = null,
            val gameGroup: String = "",
            val filterGameCount: Int = 0,
        )

        private data class Cache(
            var state: STATE = STATE.REFFED,
            val query: Query,
            val data: HashMap<Long, UserGameData>
        )

        private val cacheQueue: Array<Cache> = Array(QUEUE_SIZE) {
            Cache(STATE.INVALID, Query(-1, null, null), HashMap())
        }
        private var ptr: Int = 0

        /**
         * 캐시에서 데이터를 탐색합니다.
         * @param query 쿼리
         * @return 쿼리에 따른 결과, 캐싱되어 있지 않은 쿼리는 null 반환
         */
        private fun findData(query: Query): HashMap<Long, UserGameData>? {
            val result: HashMap<Long, UserGameData>
            val basePos: Int = ptr

            do {
                if (cacheQueue[ptr].query != query) {
                    cacheQueue[ptr].state = STATE.OLD
                    ptr = ++ptr % QUEUE_SIZE
                    continue
                }

                //CASE WHEN TARGET FOUND
                if (cacheQueue[ptr].state == STATE.INVALID) return null
                else {
                    result = cacheQueue[ptr].data
                    cacheQueue[ptr].state = STATE.REFFED
                    return result
                }
            } while (basePos != ptr)
            return null
        }

        /**
         * 데이터를 캐시화 합니다.
         * @param query 쿼리
         * @param data 쿼리에 따른 데이터
         */
        private fun insertData(query: Query, data: HashMap<Long, UserGameData>) {
            while (true) {
                if (cacheQueue[ptr].state == STATE.REFFED) {
                    cacheQueue[ptr].state = STATE.OLD
                    ptr = ++ptr % QUEUE_SIZE
                    continue
                }
                else {
                    cacheQueue[ptr] = Cache(query = query, data = data)
                    break
                }
            }
        }

        /**
         * 데이터를 가져옵니다. 캐싱된 데이터가 있다면 캐싱된 데이터를, 아니라면 DB에 연결합니다.
         * @param query 쿼리
         * @return 데이터
         * @throws DBConnectException DB연결 에러
         */
        @Throws(DBConnectException::class)
        fun getData(query: Query): HashMap<Long, UserGameData> {
            findData(query)?.let { return it }

            val dataList = DBHandler.selectGameResult(
                query.startDate,
                query.endDate,
                query.id,
                query.gameGroup,
                query.filterGameCount
            )

            val data = HashMap<Long, UserGameData>()

            dataList.forEach { userData ->
                data[userData.userID] = data[userData.userID]?.apply {
                    addGameData(userData.score, userData.rank)
                } ?: UserGameData(userData.userID).apply {
                    addGameData(userData.score, userData.rank)
                }
            }

            insertData(query, data)
            return data
        }

        /**
         * 데이터가 업데이트되었다는 것을 표시합니다.
         * @param id 서버의 id
         * @param gameGroup 게임 그룹 스트링
         */
        fun markDataToInvalid(@GuildRes id: Long, gameGroup: String) {
            for (i in cacheQueue.indices) {
                if (cacheQueue[i].query.id == id && cacheQueue[i].query.gameGroup == gameGroup)
                    cacheQueue[i].state = STATE.INVALID
            }
        }

        /**
         * 캐시를 모두 무효화 처리합니다.
         */
        fun invalidAllData() {
            cacheQueue.forEach { it.state = STATE.INVALID }
        }
    }

    /**
     * 새 반장전 결과를 추가합니다. 인덱스와 추가된 날짜가 같이 기록됩니다.
     * <br>
     * 매개변수에서 중복된 이름이 있는지, 점수의 합이 10만점인지, 점수가 정렬되어 있는지 검사합니다.
     *
     * @param game 게임의 결과 객체
     * @param gameResult 1위부터 4위의 점수가 기록된 리스트
     * @return 현재 guild && game group에서의 국 수
     * @throws ParameterErrorException 4명이 아닐 때, 점수별 정렬되어있지 않을 때, 점수 합이 10만점이 아닐 때
     * @throws GameGroupNotFoundException 등록된 game group가 아닐 때
     * @throws DBConnectException DB 처리 중 에러가 발생할 때
     */
    @Throws(ParameterErrorException::class, GameGroupNotFoundException::class, DBConnectException::class)
    fun addScore(game: Game, gameResult: Collection<GameResult>): Int {
        DataCache.markDataToInvalid(game.guildID, game.gameGroup)
        return DBHandler.addScore(game, gameResult)
    }

    /**
     * 1개월 동안의 게임 결과를 가져옵니다.
     * @param guildId 서버 id
     * @param month 검색할 달
     * @param year 검색할 년
     * @param gameGroup 검색할 게임 그룹
     * @param filterGameCount 필터링할 국 수
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun getMonthUserData(
        @GuildRes guildId: Long,
        @IntRange(1, 12) month: Int,
        year: Int,
        gameGroup: String = "",
        filterGameCount: Int = 0
    ): HashMap<Long, UserGameData> {
        val (startDate, endDate) = getTimestampForOneMonth(month, year)
        return DataCache.getData(DataCache.Query(guildId, startDate, endDate, gameGroup, filterGameCount))
    }

    /**
     * 검색 기간 동안의 게임 결과를 가져옵니다.
     * @param guildId 서버 id
     * @param startMonth 시작 월
     * @param startYear 시작 년도
     * @param endMonth 종료 월(선택한 달의 끝까지 반영 ex: 7월->7월 31일 23시 59분 59초까지 반영)
     * @param endYear 종료 년도
     * @param gameGroup 검색할 게임 그룹
     * @param filterGameCount 필터링할 국 수
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun getSelectedUserData(
        @GuildRes guildId: Long,
        @IntRange(1, 12) startMonth: Int,
        startYear: Int,
        @IntRange(1, 12) endMonth: Int,
        endYear: Int,
        gameGroup: String = "",
        filterGameCount: Int = 0
    ): HashMap<Long, UserGameData> {
        val (startDate, endDate) = getTimestampFromMonthAndYear(startMonth, startYear, endMonth, endYear)
        return DataCache.getData(DataCache.Query(guildId, startDate, endDate, gameGroup, filterGameCount))
    }

    /**
     * 모든 기간동안의 게임 결과를 가져옵니다.
     * @param guildId 서버 id
     * @param gameGroup 검색할 게임 그룹
     * @param filterGameCount 필터링할 국 수
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun getAllUserData(
        @GuildRes guildId: Long,
        gameGroup: String = "",
        filterGameCount: Int = 0
    ): HashMap<Long, UserGameData> {
        return DataCache.getData(
            DataCache.Query(
                id = guildId,
                gameGroup = gameGroup,
                filterGameCount = filterGameCount
            )
        )
    }

    /**
     * 그래프 작성용 메소드입니다. 한 달 동안의 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param guildId 검색할 서버의 id
     * @param userId 검색할 유저의 id
     * @param month 검색할 월
     * @param year 검색할 년
     * @param gameGroup 검색할 게임 그룹
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun recentMonthGameResult(
        @GuildRes guildId: Long,
        @UserRes userId: Long,
        @IntRange(1, 12) month: Int,
        year: Int,
        gameGroup: String = "",
    ): Array<IntArray> {
        val (startDate, endDate) = getTimestampForOneMonth(month, year)
        return scoreDataToNyanArray(DBHandler.selectRecentGameResult(guildId, userId, startDate, endDate, gameGroup))
    }

    /**
     * 그래프 작성용 메소드입니다. 선택한 기간 동안의 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param guildId 검색할 서버의 id
     * @param userId 검색할 유저의 id
     * @param startMonth 시작 월
     * @param startYear 시작 년도
     * @param endMonth 종료 월(선택한 달의 끝까지 반영 ex: 7월->7월 31일 23시 59분 59초까지 반영)
     * @param endYear 종료 년도
     * @param gameGroup 검색할 게임 그룹
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun recentSelectedGameResult(
        @GuildRes guildId: Long,
        @UserRes userId: Long,
        @IntRange(1, 12) startMonth: Int,
        startYear: Int,
        @IntRange(1, 12) endMonth: Int,
        endYear: Int,
        gameGroup: String = "",
    ): Array<IntArray> {
        val (startDate, endDate) = getTimestampFromMonthAndYear(startMonth, startYear, endMonth, endYear)
        return scoreDataToNyanArray(DBHandler.selectRecentGameResult(guildId, userId, startDate, endDate, gameGroup))
    }

    /**
     * 그래프 작성용 메소드입니다. 모든 기간 동안의 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param guildId 검색할 서버의 id
     * @param userId 검색할 유저의 id
     * @param gameGroup 검색할 게임 그룹
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     * @throws DBConnectException DB 연결 에러시
     */
    @Throws(DBConnectException::class)
    fun recentAllGameResult(
        @GuildRes guildId: Long,
        @UserRes userId: Long,
        gameGroup: String = "",
    ): Array<IntArray> {
        return scoreDataToNyanArray(DBHandler.selectRecentGameResult(guildId, userId, null, null, gameGroup))
    }

    fun invalidAllData() {
        DataCache.invalidAllData()
        //TODO 캐시 상태 확인 메시지 or DM -> Log 클래스 만들어서 접두사(클래스, 시간 등 정보 표시) 붙여주는 클래스..?
    }

    private fun getTimestampForOneMonth(@IntRange(1, 12) month: Int, year: Int): TimePeriod {
        val calender = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }
        val startDate = Timestamp(calender.timeInMillis)
        val endDate = Timestamp(calender.apply {
            set(Calendar.MONTH, month)
        }.timeInMillis - 1)
        return TimePeriod(startDate, endDate)
    }

    private fun getTimestampFromMonthAndYear(
        @IntRange(1, 12) startMonth: Int,
        startYear: Int,
        @IntRange(1, 12) endMonth: Int,
        endYear: Int
    ): TimePeriod {
        val startDate = Timestamp(Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, startYear)
            set(Calendar.MONTH, startMonth - 1)
        }.timeInMillis)
        val endDate = Timestamp(Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, endYear)
            set(Calendar.MONTH, endMonth)
        }.timeInMillis - 1)

        return TimePeriod(startDate, endDate)
    }

    private fun scoreDataToNyanArray(gameResult: Collection<GameResult>): Array<IntArray> {
        val result = arrayOf(
            IntArray(10) { 0 },
            IntArray(10) { 0 }
        )

        gameResult.forEachIndexed { index, game ->
            result[0][index] = game.rank
            result[1][index] = if (game.score >= 50000) 1 else 0
        }

        return result
    }


}