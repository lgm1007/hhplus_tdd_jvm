package io.hhplus.tdd.database

import io.hhplus.tdd.point.UserPoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class UserPointTableTest {
	private lateinit var userPointTable: UserPointTable

	@BeforeEach
	fun setUp() {
		userPointTable = UserPointTable()
	}

	@Test
	fun ID로_UserPoint_조회하기_디폴트값() {
        val actual = userPointTable.selectById(1L)

		validateUserPoint(actual, 1L, 0)
    }

	@Test
	fun UserPoint_삽입하기() {
		userPointTable.insertOrUpdate(1L, 100L)

		val actual = userPointTable.selectById(1L)

		validateUserPoint(actual, 1L, 100L)
	}

	@Test
	fun UserPoint_업데이트하기() {
		userPointTable.insertOrUpdate(1L, 100L)
		userPointTable.insertOrUpdate(1L, 200L)

		val actual = userPointTable.selectById(1L)


		validateUserPoint(actual, 1L, 200L)
	}

	private fun validateUserPoint(actual: UserPoint, id: Long, point: Long) {
		assertAll(
			{ assertThat(actual.id).isEqualTo(id) },
			{ assertThat(actual.point).isEqualTo(point) }
		)
	}
}