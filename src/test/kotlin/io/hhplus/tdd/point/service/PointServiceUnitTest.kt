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
	@DisplayName("포인트 충전에 대한 내역 저장 테스트")
	fun insertChargeHistoryTest() {
		val pointDtos = listOf(PointDto(1L, TransactionType.CHARGE, 100L), PointDto(1L, TransactionType.CHARGE, 200L))

		pointDtos.forEach {
			sut.chargeUserPoint(it)
		}

		val actual = sut.getAllPointHistoriesByUserId(1L)

		assertAll(
			{ assertThat(actual.size).isEqualTo(2) },
			{ assertThat(actual[0].userId).isEqualTo(1L) },
			{ assertThat(actual[0].type).isEqualTo(TransactionType.CHARGE) },
			{ assertThat(actual[0].amount).isEqualTo(100L) },
			{ assertThat(actual[1].userId).isEqualTo(1L) },
			{ assertThat(actual[1].type).isEqualTo(TransactionType.CHARGE) },
			{ assertThat(actual[1].amount).isEqualTo(200L) }
		)
	}

	@Test
	@DisplayName("포인트 충전 중 최대 잔고로 실패 예외케이스")
	fun shouldExceptionChargePointLimitOverTest() {
		val pointDto = PointDto(1L, TransactionType.CHARGE, 10_001L)

		assertThrows<PointException> {
			sut.chargeUserPoint(pointDto)
		}
	}

	@Test
	@DisplayName("포인트 사용 기능 테스트")
	fun useUserPointTest() {
		val chargePointDto = PointDto(1L, TransactionType.CHARGE, 200L)
		val usePointDto = PointDto(1L, TransactionType.USE, 100L)

		sut.chargeUserPoint(chargePointDto)
		sut.useUserPoint(usePointDto)

		val actual = sut.getUserPointById(1L)

		assertAll(
			{ assertThat(actual.id).isEqualTo(1L) },
			{ assertThat(actual.point).isEqualTo(100L) },
		)
	}

	@Test
	@DisplayName("포인트 사용에 대한 내역 저장 테스트")
	fun insertUseHistoryTest() {
		val chargePointDto = PointDto(1L, TransactionType.CHARGE, 200L)
		val usePointDto = PointDto(1L, TransactionType.USE, 100L)

		sut.chargeUserPoint(chargePointDto)
		sut.useUserPoint(usePointDto)

		val actual = sut.getAllPointHistoriesByUserId(1L)

		assertAll(
			{ assertThat(actual.size).isEqualTo(2) },
			{ assertThat(actual[1].userId).isEqualTo(1L) },
			{ assertThat(actual[1].type).isEqualTo(TransactionType.USE) },
			{ assertThat(actual[1].amount).isEqualTo(100L) },
		)
	}

	@Test
	@DisplayName("포인트 사용 중 잔고 부족으로 실패 예외케이스")
	fun shouldExceptionLeakUsePointTest() {
		val pointDto = PointDto(1L, TransactionType.USE, 100L)

		assertThrows<PointException> {
			sut.useUserPoint(pointDto)
		}
	}
}