package io.hhplus.tdd.point

data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
)

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE, USE
}