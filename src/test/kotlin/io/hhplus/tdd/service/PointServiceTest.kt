package io.hhplus.tdd.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.service.PointService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PointServiceTest {
    // Mock 객체 생성
    private val pointRepository = mockk<UserPointTable>()
    private val pointHistoryRepository = mockk<PointHistoryTable>()

    private val pointService = PointService(pointRepository, pointHistoryRepository)

    @Test
    fun `존재하는 유저 ID로 포인트를 조회하면 UserPoint 객체를 반환해야 한다`() {
        // Given
        val userId = 1L
        val expectedPoint = UserPoint(userId, 1000, System.currentTimeMillis())

        every { pointRepository.selectById(userId) } returns expectedPoint

        // When
        val result = pointService.getPoint(userId)

        // Then
        assertEquals(expectedPoint, result)

        verify(exactly = 1) { pointRepository.selectById(userId) }
    }

    @Test
    fun `존재하지 않는 유저 ID로 포인트를 조회하면 point가 0인 객체를 반환해야 한다`() {
        // Given
        val userId = 999L
        val expectedPoint = UserPoint(userId, 0, System.currentTimeMillis())

        every { pointRepository.selectById(userId) } returns expectedPoint

        // When
        val result = pointService.getPoint(userId)

        // Then
        assertEquals(expectedPoint, result)

        verify(exactly = 1) { pointRepository.selectById(userId) }
    }
}