package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.exception.PointException
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.dto.PointDto
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.*

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

	@Test
	@DisplayName("포인트 충전 최대 잔고로 실패 예외케이스")
	fun shouldChargePointExceptionTest() {
		val pointDto = PointDto(1L, TransactionType.CHARGE, 10_001L)

		assertThrows<PointException> {
			sut.chargeUserPoint(pointDto)
		}
	}
}