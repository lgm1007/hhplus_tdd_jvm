package io.hhplus.tdd.point

import io.hhplus.tdd.exception.PointException

class PointValidator {
	companion object {
		fun validateChargePointNegative(chargePoint: Long) {
			if (chargePoint < 0)
				throw PointException("충전할 포인트가 음수입니다.")
		}

		fun validatePointOverMaxLimit(amount: Long) {
			if (amount > MAX_POINT_LIMIT)
				throw PointException("충전 시 포인트가 최대 잔고입니다.")
		}

		fun validateUsePointNetagive(usePoint: Long) {
			if (usePoint < 0)
				throw PointException("사용할 포인트가 음수입니다.")
		}

		fun validatePointLackMinLimit(amount: Long) {
			if (amount < MIN_POINT_LIMIT)
				throw PointException("포인트 잔고가 부족합니다.")
		}
	}
}