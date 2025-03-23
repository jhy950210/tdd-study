package io.hhplus.tdd.database

import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Component

/**
 * 해당 Table 클래스는 변경하지 않고 공개된 API 만을 사용해 데이터를 제어합니다.
 */
@Component
class UserPointTable {
    private val table = HashMap<Long, UserPoint>()

    fun selectById(id: Long): UserPoint {
        Thread.sleep(Math.random().toLong() * 200L)
        return table[id] ?: UserPoint(id = id, point = 0, updateMillis = System.currentTimeMillis())
    }

    fun insertOrUpdate(id: Long, amount: Long): UserPoint {
        Thread.sleep(Math.random().toLong() * 300L)
        val userPoint = UserPoint(id = id, point = amount, updateMillis = System.currentTimeMillis())
        table[id] = userPoint
        return userPoint
    }
}