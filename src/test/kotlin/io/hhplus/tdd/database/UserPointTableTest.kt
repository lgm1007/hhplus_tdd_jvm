package io.hhplus.tdd.database

import io.hhplus.tdd.point.UserPoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class UserPointTableTest {
	private lateinit var sut: UserPointTable

	@BeforeEach
	fun setUp() {
		sut = UserPointTable()
	}

	/**
	 * UserPointTable의 selectById() 메서드의 기본적인 조회 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	fun ID로_UserPoint_조회하기_디폴트값() {
        val actual = sut.selectById(1L)

		validateUserPoint(actual, 1L, 0)
    }

	/**
	 * UserPointTable의 insertOrUpdate() 메서드의 UserPoint 객체 삽입 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	fun UserPoint_삽입하기() {
		sut.insertOrUpdate(1L, 100L)

		val actual = sut.selectById(1L)

		validateUserPoint(actual, 1L, 100L)
	}

	/**
	 * UserPointTable의 insertOrUpdate() 메서드의 UserPoint 객체 업데이트 기능을 검증하기 위한 테스트 코드
	 */
	@Test
	fun UserPoint_업데이트하기() {
		sut.insertOrUpdate(1L, 100L)
		sut.insertOrUpdate(1L, 200L)

		val actual = sut.selectById(1L)


		validateUserPoint(actual, 1L, 200L)
	}

	private fun validateUserPoint(actual: UserPoint, id: Long, point: Long) {
		assertAll(
			{ assertThat(actual.id).isEqualTo(id) },
			{ assertThat(actual.point).isEqualTo(point) }
		)
	}
}