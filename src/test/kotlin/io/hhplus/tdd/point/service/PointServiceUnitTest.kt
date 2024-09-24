package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.dto.PointDto
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class PointServiceUnitTest {
	private lateinit var sut: PointService

	@BeforeEach
	fun setUp() {
		sut = PointService(UserPointTable(), PointHistoryTable())
	}

	@Test
	@DisplayName("포인트 충전 기능 테스트")
	fun chargeUserPointTest() {
		val pointDtos = listOf(PointDto(1L, TransactionType.CHARGE, 100L), PointDto(1L, TransactionType.CHARGE, 200L))

		pointDtos.forEach {
			sut.chargeUserPoint(it)
		}

		val actual = sut.getUserPointById(1L)

		assertAll(
			{ assertThat(actual.id).isEqualTo(1L) },
			{ assertThat(actual.point).isEqualTo(300L) }
		)
	}
}