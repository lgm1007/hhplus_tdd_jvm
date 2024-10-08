package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class PointService(
	private val pointRepository: PointRepository,
	private val pointHistoryRepository: PointHistoryRepository,
) : PointUseCase {
	// 동시성 제어 방식을 위한 Lock ConcurrentHashMap
	// 같은 userId에 대한 요청은 Lock을 획득하여 대기하도록 하기 위함
	private val userPointLocks = ConcurrentHashMap<Long, ReentrantLock>()

	override fun getUserPointById(id: Long): UserPoint {
		return pointRepository.findById(id)
	}

	override fun getAllPointHistoriesByUserId(userId: Long): List<PointHistory> {
		return pointHistoryRepository.findAllByUserId(userId)
	}

	override fun chargeUserPoint(pointDto: PointDto): UserPoint {
		// userId가 concurrentHashMap 에 없으면 새로운 lock 획득, 있으면 존재하는 lock 가져옴
		val lock = userPointLocks.computeIfAbsent(pointDto.userId) {
			ReentrantLock(true)
		}

		return lock.withLock {
			val userPoint = getUserPointById(pointDto.userId)
			userPoint.charge(pointDto.amount)

			pointHistoryRepository.insert(
				pointDto,
				System.currentTimeMillis()
			)

			pointRepository.save(userPoint)
		}
	}

	override fun useUserPoint(pointDto: PointDto): UserPoint {
		val lock = userPointLocks.computeIfAbsent(pointDto.userId) {
			ReentrantLock(true)
		}

		return lock.withLock {
			val userPoint = getUserPointById(pointDto.userId)
			userPoint.use(pointDto.amount)

			pointHistoryRepository.insert(
				pointDto,
				System.currentTimeMillis()
			)

			pointRepository.save(userPoint)
		}
	}
}