import kuro9.mahjongbot.Setting
import kuro9.mahjongbot.db.DBHandler
import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.DefaultAsserter.assertNull
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DBConnectionTest {

    @Test
    @DisplayName("점수 추가")
    fun testAddScore() {
        var game: Game? = null
        val gameResult: MutableList<GameResult> = mutableListOf()
        val testGame = Game(guildID = 1111L, addedBy = 3L)
        val testGameResult = arrayOf(
            GameResult(userID = 1, rank = 1, score = 60000),
            GameResult(userID = 2, rank = 2, score = 40000),
            GameResult(userID = 3, rank = 3, score = 20000),
            GameResult(userID = 4, rank = 4, score = -20000)
        )
        val resultCode = DBHandler.addScore(
            testGame,
            testGameResult
        )
        val cs = db.createStatement()
        val gameRS = cs.executeQuery("SELECT * FROM Game ORDER BY id DESC LIMIT 1")
        if (gameRS.next()) {
            game = Game(
                id = gameRS.getLong("id"),
                addedBy = gameRS.getLong("added_by"),
                guildID = gameRS.getLong("guild_id"),
                gameGroup = gameRS.getString("game_group")
            )
            val gameResultRS = cs.executeQuery("SELECT * FROM GameResult WHERE game_id = ${game.id} ORDER BY user_id")
            while (gameResultRS.next()) {
                gameResult.add(
                    GameResult(
                        userID = gameResultRS.getLong("user_id"),
                        rank = gameResultRS.getInt("rank"),
                        score = gameResultRS.getInt("score")
                    )
                )
            }
        }

        assertAll(
            "adding result",
            { assertTrue("resultCode=$resultCode", resultCode > 0) },
            { assertTrue(game.toString(), game !== null && game.guildID == 1111L && game.addedBy == 3L) },
            { assertContentEquals(testGameResult, gameResult.toTypedArray()) }
        )

        if (game !== null)
            cs.executeUpdate("DELETE FROM Game WHERE id = ${game.id}")
    }

    @Test
    @DisplayName("점수 추가-파라미터 에러(GameResult size != 4)")
    fun testAddScoreParamErr1() {
        var game: Game? = null
        val gameResult: MutableList<GameResult> = mutableListOf()
        val testGame = Game(guildID = 1111L, addedBy = 3L)
        val testGameResult = arrayOf(
            GameResult(userID = 1, rank = 1, score = 60000),
            GameResult(userID = 2, rank = 2, score = 40000),
            GameResult(userID = 3, rank = 3, score = 20000)
        )
        val resultCode = DBHandler.addScore(
            testGame,
            testGameResult
        )
        val cs = db.createStatement()
        val gameRS = cs.executeQuery("SELECT * FROM Game ORDER BY id DESC LIMIT 1")
        if (gameRS.next()) {
            game = Game(
                id = gameRS.getLong("id"),
                addedBy = gameRS.getLong("added_by"),
                guildID = gameRS.getLong("guild_id"),
                gameGroup = gameRS.getString("game_group")
            )
            val gameResultRS = cs.executeQuery("SELECT * FROM GameResult WHERE game_id = ${game.id} ORDER BY user_id")
            while (gameResultRS.next()) {
                gameResult.add(
                    GameResult(
                        userID = gameResultRS.getLong("user_id"),
                        rank = gameResultRS.getInt("rank"),
                        score = gameResultRS.getInt("score")
                    )
                )
            }
        }

        assertAll(
            "adding result",
            { assertTrue("resultCode=$resultCode", resultCode == -101) },
            { assertNull("Game", game) },
            { assertTrue("GameResult", gameResult.size == 0) }
        )

        if (game !== null)
            cs.executeUpdate("DELETE FROM Game WHERE id = ${game.id}")
    }

    @Test
    @DisplayName("점수 추가-파라미터 에러(Array<GameResult> 순위 정렬 안됨)")
    fun testAddScoreParamErr2() {
        var game: Game? = null
        val gameResult: MutableList<GameResult> = mutableListOf()
        val testGame = Game(guildID = 1111L, addedBy = 3L)
        val testGameResult = arrayOf(
            GameResult(userID = 4, rank = 4, score = -20000),
            GameResult(userID = 1, rank = 1, score = 60000),
            GameResult(userID = 2, rank = 2, score = 40000),
            GameResult(userID = 3, rank = 3, score = 20000)
        )
        val resultCode = DBHandler.addScore(
            testGame,
            testGameResult
        )
        val cs = db.createStatement()
        val gameRS = cs.executeQuery("SELECT * FROM Game ORDER BY id DESC LIMIT 1")
        if (gameRS.next()) {
            game = Game(
                id = gameRS.getLong("id"),
                addedBy = gameRS.getLong("added_by"),
                guildID = gameRS.getLong("guild_id"),
                gameGroup = gameRS.getString("game_group")
            )
            val gameResultRS = cs.executeQuery("SELECT * FROM GameResult WHERE game_id = ${game.id} ORDER BY user_id")
            while (gameResultRS.next()) {
                gameResult.add(
                    GameResult(
                        userID = gameResultRS.getLong("user_id"),
                        rank = gameResultRS.getInt("rank"),
                        score = gameResultRS.getInt("score")
                    )
                )
            }
        }

        assertAll(
            "adding result",
            { assertTrue("resultCode=$resultCode", resultCode == -101) },
            { assertNull("Game", game) },
            { assertTrue("GameResult", gameResult.size == 0) }
        )

        if (game !== null)
            cs.executeUpdate("DELETE FROM Game WHERE id = ${game.id}")
    }

    @Test
    @DisplayName("점수 추가-파라미터 에러(Array<GameResult> 점수 합 != 10만점)")
    fun testAddScoreParamErr3() {
        var game: Game? = null
        val gameResult: MutableList<GameResult> = mutableListOf()
        val testGame = Game(guildID = 1111L, addedBy = 3L)
        val testGameResult = arrayOf(
            GameResult(userID = 1, rank = 1, score = 60000),
            GameResult(userID = 2, rank = 2, score = 40000),
            GameResult(userID = 3, rank = 3, score = 20000),
            GameResult(userID = 4, rank = 4, score = 20000)
        )
        val resultCode = DBHandler.addScore(
            testGame,
            testGameResult
        )
        val cs = db.createStatement()
        val gameRS = cs.executeQuery("SELECT * FROM Game ORDER BY id DESC LIMIT 1")
        if (gameRS.next()) {
            game = Game(
                id = gameRS.getLong("id"),
                addedBy = gameRS.getLong("added_by"),
                guildID = gameRS.getLong("guild_id"),
                gameGroup = gameRS.getString("game_group")
            )
            val gameResultRS = cs.executeQuery("SELECT * FROM GameResult WHERE game_id = ${game.id} ORDER BY user_id")
            while (gameResultRS.next()) {
                gameResult.add(
                    GameResult(
                        userID = gameResultRS.getLong("user_id"),
                        rank = gameResultRS.getInt("rank"),
                        score = gameResultRS.getInt("score")
                    )
                )
            }
        }

        assertAll(
            "adding result",
            { assertTrue("resultCode=$resultCode", resultCode == -101) },
            { assertNull("Game", game) },
            { assertTrue("GameResult", gameResult.size == 0) }
        )

        if (game !== null)
            cs.executeUpdate("DELETE FROM Game WHERE id = ${game.id}")
    }

    @Test
    @DisplayName("점수 추가-게임그룹 Not Exist")
    fun testAddScoreGameGroupErr() {
        var game: Game? = null
        val gameResult: MutableList<GameResult> = mutableListOf()
        val testGame = Game(guildID = 1111L, addedBy = 3L, gameGroup = "gameGroupNotExist")
        val testGameResult = arrayOf(
            GameResult(userID = 1, rank = 1, score = 60000),
            GameResult(userID = 2, rank = 2, score = 40000),
            GameResult(userID = 3, rank = 3, score = 20000),
            GameResult(userID = 4, rank = 4, score = -20000)
        )
        val resultCode = DBHandler.addScore(
            testGame,
            testGameResult
        )
        val cs = db.createStatement()
        val gameRS = cs.executeQuery("SELECT * FROM Game ORDER BY id DESC LIMIT 1")
        if (gameRS.next()) {
            game = Game(
                id = gameRS.getLong("id"),
                addedBy = gameRS.getLong("added_by"),
                guildID = gameRS.getLong("guild_id"),
                gameGroup = gameRS.getString("game_group")
            )
            val gameResultRS = cs.executeQuery("SELECT * FROM GameResult WHERE game_id = ${game.id} ORDER BY user_id")
            while (gameResultRS.next()) {
                gameResult.add(
                    GameResult(
                        userID = gameResultRS.getLong("user_id"),
                        rank = gameResultRS.getInt("rank"),
                        score = gameResultRS.getInt("score")
                    )
                )
            }
        }

        assertAll(
            "adding result",
            { assertTrue("resultCode=$resultCode", resultCode == -2) },
            { assertNull("Game", game) },
            { assertTrue("GameResult", gameResult.size == 0) }
        )

        if (game !== null)
            cs.executeUpdate("DELETE FROM Game WHERE id = ${game.id}")
    }

    @Test
    @DisplayName("게임 그룹 추가")
    fun testAddGameGroup() {
        val testGuildID = 1111L
        val testGameGroup = "GameGroupTest"
        var resultGuildID: Long? = null
        var resultGameGroup: String? = null
        val resultCode = DBHandler.addGameGroup(testGuildID, testGameGroup)

        val cs = db.createStatement()
        val groupRS =
            cs.executeQuery("SELECT * FROM GameGroup WHERE guild_id = $testGuildID AND group_name = '$testGameGroup'")
        if (groupRS.next()) {
            resultGuildID = groupRS.getLong("guild_id")
            resultGameGroup = groupRS.getString("group_name")
        }

        assertAll(
            "Game Group",
            { assertEquals(0, resultCode) },
            { assertEquals(testGuildID, resultGuildID) },
            { assertEquals(testGameGroup, resultGameGroup) }
        )

        if (resultGuildID !== null)
            cs.executeUpdate("DELETE FROM GameGroup WHERE guild_id = $resultGuildID")

    }

    @Test
    @DisplayName("게임 그룹 추가-이미 존재하는 group 추가")
    fun testAddGameGroupExistGroupErr() {
        val testGuildID = 1111L
        val testGameGroup = "GameGroupTest"
        var resultGuildID: Long? = null
        var resultGameGroup: String? = null
        DBHandler.addGameGroup(testGuildID, testGameGroup)
        val resultCode = DBHandler.addGameGroup(testGuildID, testGameGroup)

        val cs = db.createStatement()
        val groupRS =
            cs.executeQuery("SELECT * FROM GameGroup WHERE guild_id = $testGuildID AND group_name = '$testGameGroup'")
        if (groupRS.next()) {
            resultGuildID = groupRS.getLong("guild_id")
            resultGameGroup = groupRS.getString("group_name")
        }

        assertAll(
            "Game Group",
            { assertEquals(-1, resultCode) },
            { assertEquals(testGuildID, resultGuildID) },
            { assertEquals(testGameGroup, resultGameGroup) }
        )

        if (resultGuildID !== null)
            cs.executeUpdate("DELETE FROM GameGroup WHERE guild_id = $resultGuildID")

    }

    @Test
    @DisplayName("게임 그룹 추가-group 이름 규칙 불일치")
    fun testAddGameGroupParamErr() {
        val testGuildID = 1111L
        val testGameGroup = "GameGroup Test"
        var resultGuildID: Long? = null
        var resultGameGroup: String? = null
        val resultCode = DBHandler.addGameGroup(testGuildID, testGameGroup)

        val cs = db.createStatement()
        val groupRS =
            cs.executeQuery("SELECT * FROM GameGroup WHERE guild_id = $testGuildID AND group_name = '$testGameGroup'")
        if (groupRS.next()) {
            resultGuildID = groupRS.getLong("guild_id")
            resultGameGroup = groupRS.getString("group_name")
        }

        assertAll(
            "Game Group",
            { assertEquals(-101, resultCode) },
            { assertNull("guildID", resultGuildID) },
            { assertNull("group", resultGameGroup) }
        )

        if (resultGuildID !== null)
            cs.executeUpdate("DELETE FROM GameGroup WHERE guild_id = $resultGuildID")

    }

    companion object {
        val db: Connection

        init {
            Setting.init()

            Class.forName("com.mysql.cj.jdbc.Driver")
            db = DriverManager.getConnection(
                Setting.DB_URL,
                Setting.DB_USER,
                Setting.DB_PASSWORD
            )
        }
    }
}