package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.TransactionType

data class PointDto(
	val userId: Long,
	val type: TransactionType,
	val amount: Long,
)