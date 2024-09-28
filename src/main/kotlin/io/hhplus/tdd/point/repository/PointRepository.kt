package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.UserPoint

interface PointRepository {
	fun findById(id: Long): UserPoint

	fun save(userPoint: UserPoint): UserPoint
}