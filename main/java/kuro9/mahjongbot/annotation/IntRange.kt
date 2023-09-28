package kuro9.mahjongbot.annotation


@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class IntRange(
    val inclusiveStart: Long = Long.MIN_VALUE,
    val inclusiveEnd: Long = Long.MAX_VALUE
)