package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.validate.PointValidator
import org.springframework.stereotype.Service

@Service
class PointService(
	private val userPointTable: UserPointTable,
	private val pointHistoryTable: PointHistoryTable,
) : PointUseCase {

	override fun getUserPointById(id: Long): UserPoint {
		return userPointTable.selectById(id)
	}

	override fun getAllPointHistoriesByUserId(userId: Long): List<PointHistory> {
		return pointHistoryTable.selectAllByUserId(userId)
	}

	// 동시성 제어를 위해 Synchronized method로 정의하여 하나의 스레드 씩 참조하도록 함
	@Synchronized
	override fun chargeUserPoint(pointDto: PointDto): UserPoint {
		val point = userPointTable.selectById(pointDto.userId).point
		val afterChargeAmount = point + pointDto.amount

		// 유저가 현재 보유한 포인트 + 충전하고자 하는 포인트가 최대 잔고를 넘는지 검사
		PointValidator.validatePointOverMaxLimit(afterChargeAmount)

		pointHistoryTable.insert(
			pointDto.userId,
			pointDto.amount,
			pointDto.type,
			System.currentTimeMillis()
		)

		return userPointTable.insertOrUpdate(pointDto.userId, afterChargeAmount)
	}

	@Synchronized
	override fun useUserPoint(pointDto: PointDto): UserPoint {
		val point = userPointTable.selectById(pointDto.userId).point
		val afterUseAmount = point - pointDto.amount

		// 유저가 현재 보유한 포인트 - 사용하고자 하는 포인트가 최소 포인트 값보다 작은지 검사 (잔고 부족)
		PointValidator.validatePointLackMinLimit(afterUseAmount)

		pointHistoryTable.insert(
			pointDto.userId,
			pointDto.amount,
			pointDto.type,
			System.currentTimeMillis()
		)

		return userPointTable.insertOrUpdate(pointDto.userId, afterUseAmount)
	}
}