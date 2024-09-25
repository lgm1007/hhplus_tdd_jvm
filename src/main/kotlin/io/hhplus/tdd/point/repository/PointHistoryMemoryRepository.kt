package io.hhplus.tdd.point.repository

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.dto.PointDto
import org.springframework.stereotype.Repository

@Repository
class PointHistoryMemoryRepository(private val pointHistoryTable: PointHistoryTable) : PointHistoryRepository {
	override fun findAllByUserId(userId: Long): List<PointHistory> {
		return pointHistoryTable.selectAllByUserId(userId)
	}

	override fun insert(pointDto: PointDto, updateMillis: Long): PointHistory {
		return pointHistoryTable.insert(pointDto.userId, pointDto.amount, pointDto.type, updateMillis)
	}
}