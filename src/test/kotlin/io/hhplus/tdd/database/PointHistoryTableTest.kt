package io.hhplus.tdd.database

import io.hhplus.tdd.point.TransactionType
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointHistoryTableTest {
	private lateinit var pointHistoryTable: PointHistoryTable

	@BeforeEach
	fun setUp() {
		pointHistoryTable = PointHistoryTable()
	}

	/**
	 * PointHistoryTable의 selectAllByUserId() 메서드의 기본 조회 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	fun ID로_PointHistory_모두_조회하기_디폴트값() {
		val actual = pointHistoryTable.selectAllByUserId(1L)

		assertThat(actual.size).isEqualTo(0)
	}

	/**
	 * PointHistoryTable의 insert() 메서드의 데이터 삽입 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	fun PointHistory_삽압하기() {
		pointHistoryTable.insert(1L, 200L, TransactionType.CHARGE, System.currentTimeMillis())
		pointHistoryTable.insert(1L, 100L, TransactionType.USE, System.currentTimeMillis())

		val actual = pointHistoryTable.selectAllByUserId(1L)

		assertThat(actual.size).isEqualTo(2)
	}
}