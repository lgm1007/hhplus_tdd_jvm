package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    var point: Long,
    val updateMillis: Long,
) {
    fun charge(chargePoint: Long) {
        PointValidator.validateChargePointNegative(chargePoint)
        PointValidator.validatePointOverMaxLimit(point + chargePoint)
        point += chargePoint
    }

    fun use(usePoint: Long) {
        PointValidator.validateUsePointNetagive(usePoint)
        PointValidator.validatePointLackMinLimit(point - usePoint)
        point -= usePoint
    }
}
