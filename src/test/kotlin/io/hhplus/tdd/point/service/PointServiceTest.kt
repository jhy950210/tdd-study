package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import io.mockk.*
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

class PointServiceTest {
    private lateinit var pointRepository: UserPointTable
    private lateinit var pointHistoryRepository: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        pointRepository = mockk<UserPointTable>()
        pointHistoryRepository = mockk<PointHistoryTable>()
        pointService = PointService(pointRepository, pointHistoryRepository)
    }

    @Test
    fun `조회 성공 - 존재하는 유저 ID로 포인트를 조회하면 UserPoint 객체를 반환`() {
        // Given
        val userId = 1L
        val expectedPoint = UserPoint(userId, 1000, System.currentTimeMillis())

        every { pointRepository.selectById(userId) } returns expectedPoint

        // When
        val result = pointService.get(userId)

        // Then
        assertEquals(expectedPoint, result)

        verify(exactly = 1) { pointRepository.selectById(userId) }
    }

    @Test
    fun `충전 실패 - 0 이하의 금액 충전 시 예외 발생`() {
        // Given
        val userId = 1L
        val chargeAmount = 0L

        // When & Then
        val exception = assertThrows<BadRequestException> {
            pointService.charge(userId, chargeAmount)
        }
        assertEquals("0과 같거나 적은 값은 충전이 불가합니다.", exception.message)

        verify { pointRepository wasNot Called }
        verify { pointHistoryRepository wasNot Called }
    }

    @Test
    fun `충전 실패 - 포인트 한도 초과 시 예외 발생`() {
        // Given
        val userId = 1L
        val existingPoint = 9000L
        val chargeAmount = 1001L
        val userPoint = UserPoint(userId, existingPoint, System.currentTimeMillis())

        every { pointRepository.selectById(userId) } returns userPoint

        // When & Then
        val exception = assertThrows<BadRequestException> {
            pointService.charge(userId, chargeAmount)
        }
        assertEquals("포인트 한계 금액을 초과하였습니다.", exception.message)

        verify(exactly = 1) { pointRepository.selectById(userId) }
        verify(exactly = 0) { pointRepository.insertOrUpdate(userId, any()) }
        verify { pointHistoryRepository wasNot Called }
    }

    @Test
    fun `충전 성공 - UserPoint 객체 반환`() {
        // Given
        val userId = 1L
        val existingPoint = 9000L
        val chargeAmount = 1000L
        val userPoint = UserPoint(userId, existingPoint, System.currentTimeMillis())
        val updatedUserPoint = UserPoint(userId, existingPoint + chargeAmount, System.currentTimeMillis())
        val historyId = 1L
        val pointHistory = PointHistory(historyId, userId, TransactionType.CHARGE, chargeAmount, updatedUserPoint.updateMillis)

        every { pointRepository.selectById(userId) } returns userPoint
        every { pointRepository.insertOrUpdate(userId, existingPoint + chargeAmount) } returns updatedUserPoint
        every { pointHistoryRepository.insert(userId, chargeAmount, TransactionType.CHARGE, updatedUserPoint.updateMillis) } returns pointHistory

        // When
        val result = pointService.charge(userId, chargeAmount)

        // Then
        assertEquals(result.point, updatedUserPoint.point)
        verify(exactly = 1) { pointRepository.selectById(userId) }
        verify(exactly = 1) { pointRepository.insertOrUpdate(userId, any()) }
        verify(exactly = 1) { pointHistoryRepository.insert(userId, any(), any(), any()) }
    }

    @Test
    fun `포인트 사용 실패 - 0 이하의 금액 사용 시 예외 발생`() {
        // Given
        val userId = 1L
        val useAmount = 0L

        // When & Then
        val exception = assertThrows<BadRequestException> {
            pointService.use(userId, useAmount)
        }
        assertEquals("0과 같거나 적은 값은 사용이 불가합니다.", exception.message)

        verify { pointRepository wasNot Called }
        verify { pointHistoryRepository wasNot Called }
    }

    @Test
    fun `포인트 사용 실패 - 보유 포인트 초과 시 예외 발생`() {
        // Given
        val userId = 1L
        val useAmount = 5000L
        val userPoint = UserPoint(userId, 3000L, System.currentTimeMillis())

        every { pointRepository.selectById(userId) } returns userPoint

        // When & Then
        val exception = assertThrows<BadRequestException> {
            pointService.use(userId, useAmount)
        }
        assertEquals("보유한 포인트를 초과하였습니다.", exception.message)

        verify(exactly = 1) { pointRepository.selectById(userId) }
        verify(exactly = 0) { pointRepository.insertOrUpdate(userId, any()) }
        verify { pointHistoryRepository wasNot Called }
    }

    @Test
    fun `포인트 사용 성공 - 정상적인 경우 포인트 업데이트와 기록 삽입`() {
        // Given
        val userId = 1L
        val useAmount = 3000L
        val userPoint = UserPoint(userId, 3000L, System.currentTimeMillis())
        val expectedRemainPoint = 0L
        val historyId = 1L
        val pointHistory = PointHistory(historyId, userId, TransactionType.USE, useAmount, userPoint.updateMillis)

        every { pointRepository.selectById(userId) } returns userPoint
        every { pointRepository.insertOrUpdate(userId, expectedRemainPoint) } returns userPoint.copy(point = expectedRemainPoint)
        every { pointHistoryRepository.insert(userId, useAmount, TransactionType.USE, any()) } returns pointHistory

        // When
        val result = pointService.use(userId, useAmount)

        // Then
        assertEquals(expectedRemainPoint, result.point)
        verify(exactly = 1) {
            pointRepository.selectById(userId)
            pointRepository.insertOrUpdate(userId, expectedRemainPoint)
            pointHistoryRepository.insert(userId, useAmount, TransactionType.USE, any())
        }
    }

}