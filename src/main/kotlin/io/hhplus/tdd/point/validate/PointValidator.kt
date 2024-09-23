package io.hhplus.tdd.point.validate

import io.hhplus.tdd.exception.PointException
import io.hhplus.tdd.point.MAX_POINT_LIMIT
import io.hhplus.tdd.point.MIN_POINT_LIMIT

class PointValidator {
	companion object {
		fun validatePointOverMaxLimit(amount: Long) {
			if (amount > MAX_POINT_LIMIT)
				throw PointException("충전 시 포인트가 최대 잔고입니다.")
		}

		fun validatePointLackMinLimit(amount: Long) {
			if (amount < MIN_POINT_LIMIT)
				throw PointException("포인트 잔고가 부족합니다.")
		}
	}
}