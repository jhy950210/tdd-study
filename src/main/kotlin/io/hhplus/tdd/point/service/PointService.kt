package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: UserPointTable,
    private val pointHistoryRepository: PointHistoryTable,
) {
    fun get(id: Long): UserPoint {
        return pointRepository.selectById(id)
    }

    fun charge(id: Long, amount: Long): UserPoint {
        if (amount <= 0L) throw BadRequestException("0과 같거나 적은 값은 충전이 불가합니다.")

        val foundUserPoint = this.pointRepository.selectById(id)
        val newTotalPoint = amount + foundUserPoint.point

        if (newTotalPoint > UserPoint.POINT_LIMIT) throw BadRequestException("포인트 한계 금액을 초과하였습니다.")

        val updatedUserPoint = this.pointRepository.insertOrUpdate(id, newTotalPoint)
        this.pointHistoryRepository.insert(id, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis)

        return updatedUserPoint
    }

    fun use(id: Long, amount: Long): UserPoint {
        if (amount <= 0L) throw BadRequestException("0과 같거나 적은 값은 사용이 불가합니다.")

        val foundUserPoint = this.pointRepository.selectById(id)
        val remainPoint = foundUserPoint.point - amount

        if (remainPoint < 0L) throw BadRequestException("보유한 포인트를 초과하였습니다.")

        val updatedUserPoint = this.pointRepository.insertOrUpdate(id, remainPoint)
        this.pointHistoryRepository.insert(id, amount, TransactionType.USE, updatedUserPoint.updateMillis)

        return updatedUserPoint
    }

    fun getHistories(id: Long): List<PointHistory> {
        return this.pointHistoryRepository.selectAllByUserId(id)
    }
}