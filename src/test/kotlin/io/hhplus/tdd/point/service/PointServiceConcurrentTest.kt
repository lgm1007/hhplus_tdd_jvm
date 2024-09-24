package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class PointServiceConcurrentTest {
	private lateinit var userPointTable: UserPointTable
	private lateinit var pointHistoryTable: PointHistoryTable
	private lateinit var sut: PointService

	@BeforeEach
	fun setUp() {
		userPointTable = UserPointTable()
		pointHistoryTable = PointHistoryTable()
		sut = PointService(userPointTable, pointHistoryTable)
	}

	@Test
	@DisplayName("포인트 충전 병렬 요청에 대한 동시성 제어 기능 테스트")
	fun handleChargePointConcurrentTest() {
		// 최대 10개의 스레드를 가진 스레드 풀 생성
		val executor = Executors.newFixedThreadPool(10)

		try {
			val futures = mutableListOf<CompletableFuture<UserPoint>>()

			// 아이디 1L인 유저의 다중 포인트 충전 요청
			repeat(10) {
				val future = CompletableFuture.supplyAsync({
					sut.chargeUserPoint(PointDto(1L, TransactionType.CHARGE, 100L))
			    }, executor)
				futures.add(future)
			}

			// 아이디 2L인 유저의 다중 포인트 충전 요청
			repeat(10) {
				val future = CompletableFuture.supplyAsync({
					sut.chargeUserPoint(PointDto(2L, TransactionType.CHARGE, 100L))
				}, executor)
				futures.add(future)
			}

			// 모든 작업이 완료될 때까지 대기
			futures.forEach {
				it.join()
			}

			val user1Point = userPointTable.selectById(1L).point
			val user2Point = userPointTable.selectById(2L).point

			val user1Histories = pointHistoryTable.selectAllByUserId(1L)
			val user2Histories = pointHistoryTable.selectAllByUserId(2L)

			assertAll(
				{ assertThat(user1Point).isEqualTo(1000L) },
				{ assertThat(user2Point).isEqualTo(1000L) },
				{ assertThat(user1Histories.size).isEqualTo(10) },
				{ assertThat(user2Histories.size).isEqualTo(10) }
			)
		} finally {
			executor.shutdown()
		}
	}
}