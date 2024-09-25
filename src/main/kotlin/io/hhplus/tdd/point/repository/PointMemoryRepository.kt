package io.hhplus.tdd.point.repository

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Repository

@Repository
class PointMemoryRepository(private val userPointTable: UserPointTable) : PointRepository {
	override fun findById(id: Long): UserPoint {
		return userPointTable.selectById(id)
	}

	override fun save(userPoint: UserPoint): UserPoint {
		return userPointTable.insertOrUpdate(userPoint.id, userPoint.point)
	}
}