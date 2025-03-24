package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: UserPointTable,
    private val pointHistoryRepository: PointHistoryTable,
) {

    fun getPoint(id: Long): UserPoint {
        return pointRepository.selectById(id)
    }
}