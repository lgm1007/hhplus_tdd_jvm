package io.hhplus.tdd.point.service

import io.hhplus.tdd.exception.PointException
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.*

class PointServiceUnitTest {
	private lateinit var sut: PointService

	@BeforeEach
	fun setUp() {
		sut = PointService(FakePointRepository(), FakePointHistoryRepository())
	}

	@Test
	@DisplayName("포인트 조회 기능 테스트")
	fun getUserPointTest() {
		val actual = sut.getUserPointById(1L)

		assertAll(
			{ assertThat(actual.id).isEqualTo(1L) },
			{ assertThat(actual.point).isEqualTo(0) },
		)
	}

	@Test
	@DisplayName("포인트 100, 200 충전 기능 테스트")
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
	@DisplayName("포인트 충전에 대한 내역 조회 기능 테스트")
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

		assertThatThrownBy { sut.chargeUserPoint(pointDto) }
			.isInstanceOf(PointException::class.java)
			.hasMessage("충전 시 포인트가 최대 잔고입니다.")
	}

	@Test
	@DisplayName("포인트 충전 중 충전할 포인트가 음수 값일 경우 실패 예외케이스")
	fun shouldExceptionChargePointNegativeTest() {
		val pointDto = PointDto(1L, TransactionType.CHARGE, -1L)

		assertThatThrownBy { sut.chargeUserPoint(pointDto) }
			.isInstanceOf(PointException::class.java)
			.hasMessage("충전할 포인트가 음수입니다.")
	}

	@Test
	@DisplayName("포인트 200 충전 후 100 사용 기능 테스트")
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
	@DisplayName("포인트 사용에 대한 내역 조회 기능 테스트")
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

		assertThatThrownBy { sut.useUserPoint(pointDto) }
			.isInstanceOf(PointException::class.java)
			.hasMessage("포인트 잔고가 부족합니다.")
	}

	@Test
	@DisplayName("포인트 사용 중 사용할 포인트가 음수 값일 경우 실패 예외케이스")
	fun shouldExceptionUsePointNegativeTest() {
		val pointDto = PointDto(1L, TransactionType.USE, -1L)

		assertThatThrownBy { sut.useUserPoint(pointDto) }
			.isInstanceOf(PointException::class.java)
			.hasMessage("사용할 포인트가 음수입니다.")
	}
}

class FakePointRepository : PointRepository {
	private val table = HashMap<Long, UserPoint>()

	override fun findById(id: Long): UserPoint {
		Thread.sleep(Math.random().toLong() * 200L)
		return table[id] ?: UserPoint(id = id, point = 0, updateMillis = System.currentTimeMillis())
	}

	override fun save(userPoint: UserPoint): UserPoint {
		Thread.sleep(Math.random().toLong() * 300L)
		val userPoint = UserPoint(id = userPoint.id, point = userPoint.point, updateMillis = System.currentTimeMillis())
		table[userPoint.id] = userPoint
		return userPoint
	}
}

class FakePointHistoryRepository : PointHistoryRepository {
	private val table = mutableListOf<PointHistory>()
	private var cursor: Long = 1L

	override fun findAllByUserId(userId: Long): List<PointHistory> {
		return table.filter { it.userId == userId }
	}

	override fun insert(pointDto: PointDto, updateMillis: Long): PointHistory {
		Thread.sleep(Math.random().toLong() * 300L)
		val history = PointHistory(
			id = cursor++,
			userId = pointDto.userId,
			amount = pointDto.amount,
			type = pointDto.type,
			timeMillis = updateMillis,
		)
		table.add(history)
		return history
	}
}