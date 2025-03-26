package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PointServiceIntegrationTest {
    @Autowired
    private lateinit var pointService: PointService

    @Test
    fun `포인트 충전 성공 - 포인트 충전시 UserPoint의 amount 증가 & 충전 History 생성`() {
        // Given
        // 충전
        val userId = 1L
        val point = 1000L
        pointService.charge(userId, point)

        // When
        val userPoint: UserPoint = pointService.get(userId)
        val histories: List<PointHistory> = pointService.getHistories(userId)

        // Then
        assertEquals(histories.size, 1)

        assertEquals(userPoint.point, point)
        val chargeHistory = histories[0]
        assertEquals(chargeHistory.userId, userId)
        assertEquals(chargeHistory.type, TransactionType.CHARGE)
        assertEquals(chargeHistory.amount, point)
    }

    @Test
    fun `포인트 사용 성공 - 포인트 사용시 UserPoint의 amount 감소 & 사용 History 생성`() {
        // Given
        // 충전
        val userId = 1L
        val chargedPoint = 1000L
        val usedPoint = 500L
        pointService.charge(userId, chargedPoint)
        pointService.use(userId, usedPoint)

        // When
        val userPoint: UserPoint = pointService.get(userId)
        val histories: List<PointHistory> = pointService.getHistories(userId)

        // Then
        assertEquals(histories.size, 2)

        assertEquals(userPoint.point, chargedPoint - usedPoint)
        val useHistory = histories[1]
        assertEquals(useHistory.userId, userId)
        assertEquals(useHistory.type, TransactionType.USE)
        assertEquals(useHistory.amount, usedPoint)
    }

    @Test
    fun `포인트 내역 조회 성공 - 유저가 포인트 충전&사용 이력이 없으면 emptyList 반환`() {
        // When
        val userId = 1L
        val result: List<PointHistory> = pointService.getHistories(userId)

        // Then
        assertEquals(result, emptyList<PointHistory>())
    }

    @Test
    fun `포인트 내역 조회 성공 - 유저가 포인트 충전 이력이 있으면 {transactionType = CHARGE, amount = 충전금액}을 가진 객체를 담은 List 반환`() {
        // Given
        // 충전
        val userId = 1L
        val point = 1000L
        pointService.charge(userId, point)

        // When
        val result: List<PointHistory> = pointService.getHistories(userId)

        // Then
        assertEquals(result.size, 1)

        val chargeHistory = result[0]
        assertEquals(chargeHistory.userId, userId)
        assertEquals(chargeHistory.type, TransactionType.CHARGE)
        assertEquals(chargeHistory.amount, point)
    }

    @Test
    fun `포인트 내역 조회 성공 - 유저가 포인트 사용 이력이 있으면 {transactionType = USE, amount = 사용금액}을 가진 객체를 담은 List 반환`() {
        // Given
        // 1. 충전
        val userId = 1L
        val chargedPoint = 1000L
        pointService.charge(userId, chargedPoint)
        // 2. 사용
        val usedPoint = 500L
        pointService.use(userId, usedPoint)

        // When
        val result: List<PointHistory> = pointService.getHistories(userId)

        // Then
        assertEquals(result.size, 2)

        val chargeHistory = result[0]
        val useHistory = result[1]
        assertEquals(chargeHistory.userId, userId)
        assertEquals(chargeHistory.type, TransactionType.CHARGE)
        assertEquals(useHistory.userId, userId)
        assertEquals(useHistory.type, TransactionType.USE)
        assertEquals(useHistory.amount, usedPoint)
    }
}