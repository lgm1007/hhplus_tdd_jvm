package io.hhplus.tdd.database

import io.hhplus.tdd.point.TransactionType
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class PointHistoryTableTest {
	private lateinit var sut: PointHistoryTable

	@BeforeEach
	fun setUp() {
		sut = PointHistoryTable()
	}

	/**
	 * PointHistoryTable의 selectAllByUserId() 메서드의 기본 조회 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	@DisplayName("ID로 PointHistory 모두 조회하는 기능 테스트")
	fun selectAllPointHistoryByUserIdTest() {
		val actual = sut.selectAllByUserId(1L)

		assertThat(actual.size).isEqualTo(0)
	}

	/**
	 * PointHistoryTable의 insert() 메서드의 데이터 삽입 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	@DisplayName("PointHistory 삽압하는 기능 테스트")
	fun insertPointHistoryTest() {
		sut.insert(1L, 200L, TransactionType.CHARGE, System.currentTimeMillis())
		sut.insert(1L, 100L, TransactionType.USE, System.currentTimeMillis())

		val actual = sut.selectAllByUserId(1L)

		assertAll(
			{ assertThat(actual.size).isEqualTo(2) },
			{ assertThat(actual[0].userId).isEqualTo(1L) },
			{ assertThat(actual[0].amount).isEqualTo(200L) },
			{ assertThat(actual[0].type).isEqualTo(TransactionType.CHARGE) },
			{ assertThat(actual[1].userId).isEqualTo(1L) },
			{ assertThat(actual[1].amount).isEqualTo(100L) },
			{ assertThat(actual[1].type).isEqualTo(TransactionType.USE) }
		)
	}
}