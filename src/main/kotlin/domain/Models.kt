package it.fraudata.domain

import it.fraudata.util.BigDecimalSerializer
import it.fraudata.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.sql.RowId
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

@Serializable
enum class TransactionType {
    DEPOSIT, WITHDRAWAL
}
@JvmInline
@Serializable

value class CountryCode(val value: String) {
    init {
        require(value.length == 2) { "the code must have 2 letters length" }
    }
}

@Serializable
data class Account(
    val id : String = UUID.randomUUID().toString(),
    val customerName : String,
    @Serializable(with = BigDecimalSerializer::class) // Custom serializer
    val balance : BigDecimal,
    val countryCode: CountryCode = CountryCode("IT")
)

@Serializable
data class Transaction(
    val id : String = UUID.randomUUID().toString(),
    val accountId: String,
    @Serializable(with = BigDecimalSerializer::class) // Custom serializer
    val amount : BigDecimal,
    val type : TransactionType,
    @Serializable(with = LocalDateTimeSerializer::class) // Custom serializer
    val timestamp : LocalDateTime = LocalDateTime.now(),
    val merchantName : String? = null,
    val isFraudulent: Boolean = false)