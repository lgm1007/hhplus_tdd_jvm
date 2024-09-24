package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.validate.PointValidator
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class PointService(
	private val userPointTable: UserPointTable,
	private val pointHistoryTable: PointHistoryTable,
) : PointUseCase {
	// 동시성 제어 방식을 위한 Lock ConcurrentHashMap
	// 같은 userId에 대한 요청은 Lock을 획득하여 대기하도록 하기 위함
	private val userPointLocks = ConcurrentHashMap<Long, ReentrantLock>()

	override fun getUserPointById(id: Long): UserPoint {
		return userPointTable.selectById(id)
	}

	override fun getAllPointHistoriesByUserId(userId: Long): List<PointHistory> {
		return pointHistoryTable.selectAllByUserId(userId)
	}

	override fun chargeUserPoint(pointDto: PointDto): UserPoint {
		// userId가 concurrentHashMap 에 없으면 새로운 lock 획득, 있으면 존재하는 lock 가져옴
		val lock = userPointLocks.computeIfAbsent(pointDto.userId) {
			ReentrantLock()
		}

		return lock.withLock {
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

			userPointTable.insertOrUpdate(pointDto.userId, afterChargeAmount)
		}
	}

	override fun useUserPoint(pointDto: PointDto): UserPoint {
		val lock = userPointLocks.computeIfAbsent(pointDto.userId) {
			ReentrantLock()
		}

		return lock.withLock {
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

			userPointTable.insertOrUpdate(pointDto.userId, afterUseAmount)
		}
	}
}