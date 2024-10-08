package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.dto.PointDto
import io.hhplus.tdd.point.service.PointUseCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointUseCase: PointUseCase
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 유저의 포인트를 조회하는 기능
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return pointUseCase.getUserPointById(id)
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointUseCase.getAllPointHistoriesByUserId(id)
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointUseCase.chargeUserPoint(PointDto(id, TransactionType.CHARGE, amount))
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointUseCase.useUserPoint(PointDto(id, TransactionType.USE, amount))
    }
}