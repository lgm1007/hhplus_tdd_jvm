package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.dto.PointDto

interface PointHistoryRepository {
	fun findAllByUserId(userId: Long): List<PointHistory>

	fun insert(pointDto: PointDto, updateMillis: Long): PointHistory
}