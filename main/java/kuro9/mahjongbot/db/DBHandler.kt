package kuro9.mahjongbot.db

import kuro9.mahjongbot.db.data.Game
import kuro9.mahjongbot.db.data.GameResult
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

object DBHandler {
    private val db: Connection?
    private val isConnected: Boolean
        get() = (db === null)
    private val addScorePS: PreparedStatement?

    init {
        Class.forName("com.mysql.jdbc.Driver")
        db = DriverManager.getConnection("jdbc:mysql://localhost:3306/Mahjong_record")
        if (!isConnected) throw SQLException()
        addScorePS =
            db?.prepareStatement("INSERT INTO Mahjong_record.Game(guild_id, game_group, added_by, created_at) VALUES (?, ?, ?, ?); INSERT INTO Mahjong_record.GameResult(game_id, user_id, `rank`, score) VALUES ((SELECT LAST_INSERT_ID()), ?, ?, ?)")
    }

    fun addScore(game: Game, result: Array<GameResult>): Int {
        if (!isConnected) return -1
        return 0
    }
}