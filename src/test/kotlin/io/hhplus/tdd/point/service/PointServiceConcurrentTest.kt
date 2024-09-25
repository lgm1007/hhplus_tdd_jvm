package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.repository.PointHistoryMemoryRepository
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointMemoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class PointServiceConcurrentTest {
	private lateinit var pointRepository: PointRepository
	private lateinit var pointHistoryRepository: PointHistoryRepository
	private lateinit var sut: PointService

	@BeforeEach
	fun setUp() {
		pointRepository = PointMemoryRepository(UserPointTable())
		pointHistoryRepository = PointHistoryMemoryRepository(PointHistoryTable())
		sut = PointService(pointRepository, pointHistoryRepository)
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

			val user1Point = pointRepository.findById(1L).point
			val user2Point = pointRepository.findById(2L).point

			val user1Histories = pointHistoryRepository.findAllByUserId(1L)
			val user2Histories = pointHistoryRepository.findAllByUserId(2L)

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

	@Test
	@DisplayName("포인트 충전 및 사용의 병렬 요청에 대해 동일한 유저에 대한 요청의 순차성 보장 테스트")
	fun handleChargeAndUsePointConcurrentTest() {
		// 최대 10개의 스레드를 가진 스레드 풀 생성
		val executor = Executors.newFixedThreadPool(10)

		try {
			// 아이디 1L인 유저의 포인트 충전 & 사용 요청
			// 순서: 700 충전 -> 300 사용 -> 400 사용 -> 500 충전 -> 200 사용
			val user1Future = CompletableFuture.completedFuture(Unit)
				.thenCompose {
					CompletableFuture.supplyAsync({
						sut.chargeUserPoint(PointDto(1L, TransactionType.CHARGE, 700L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(1L, TransactionType.USE, 300L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(1L, TransactionType.USE, 400L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.chargeUserPoint(PointDto(1L, TransactionType.CHARGE, 500L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(1L, TransactionType.USE, 200L))
					}, executor)
				}

			// 아이디 2L인 유저의 포인트 충전 & 사용 요청
			// 순서: 500 충전 -> 300 사용 -> 200 사용 -> 600 충전 -> 400 사용
			val user2Future = CompletableFuture.completedFuture(Unit)
				.thenCompose {
					CompletableFuture.supplyAsync({
						sut.chargeUserPoint(PointDto(2L, TransactionType.CHARGE, 500L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(2L, TransactionType.USE, 300L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(2L, TransactionType.USE, 200L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.chargeUserPoint(PointDto(2L, TransactionType.CHARGE, 600L))
					}, executor)
				}.thenCompose {
					CompletableFuture.supplyAsync({
						sut.useUserPoint(PointDto(2L, TransactionType.USE, 400L))
					}, executor)
				}

			// 모든 작업이 완료될 때까지 대기
			user1Future.join()
			user2Future.join()

			// 일정 시간 작업이 완료될 때까지 대기
			Thread.sleep(5000)

			val user1Point = pointRepository.findById(1L).point
			val user2Point = pointRepository.findById(2L).point

			val user1Histories = pointHistoryRepository.findAllByUserId(1L)
			val user2Histories = pointHistoryRepository.findAllByUserId(2L)

			assertAll(
				{ assertThat(user1Point).isEqualTo(300L) },
				{ assertThat(user2Point).isEqualTo(200L) },
				{ assertThat(user1Histories.size).isEqualTo(5) },
				{ assertThat(user2Histories.size).isEqualTo(5) }
			)
		} finally {
			executor.shutdown()
		}
	}
}