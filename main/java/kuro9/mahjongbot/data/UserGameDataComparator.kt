package kuro9.mahjongbot.data

fun compareWithUma(dataA: UserGameData, dataB: UserGameData) = Math.toIntExact(dataB.totalUmaLong - dataA.totalUmaLong)

fun compareWithGameCount(dataA: UserGameData, dataB: UserGameData) = dataB.gameCount - dataA.gameCount

fun compareWithTobi(dataA: UserGameData, dataB: UserGameData) =
    ((dataB.rankPercentage[4] * 100) - (dataA.rankPercentage[4] * 100)).toInt()

fun compareWithAvgRank(dataA: UserGameData, dataB: UserGameData) =
    ((dataA.avgRank * 100) - (dataB.avgRank * 100)).toInt()

fun compareWithAvgUma(dataA: UserGameData, dataB: UserGameData) = ((dataB.avgUma * 100) - (dataA.avgUma * 100)).toInt()