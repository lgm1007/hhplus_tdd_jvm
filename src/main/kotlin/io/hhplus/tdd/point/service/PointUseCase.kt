package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto

interface PointUseCase {
	/**
	 * 특정 유저의 포인트를 조회하는 기능
	 */
	fun getUserPointById(id: Long): UserPoint

	/**
	 * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
	 */
	fun getAllPointHistoriesByUserId(userId: Long): List<PointHistory>

	/**
	 * 특정 유저의 포인트를 충전하는 기능
	 */
	fun chargeUserPoint(pointDto: PointDto): UserPoint

	/**
	 * 특정 유저의 포인트를 사용하는 기능
	 */
	fun useUserPoint(pointDto: PointDto): UserPoint
}