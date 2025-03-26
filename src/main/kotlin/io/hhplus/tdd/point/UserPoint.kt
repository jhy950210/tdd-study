package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
){
    companion object {
        const val POINT_LIMIT = 10000L
    }
}
