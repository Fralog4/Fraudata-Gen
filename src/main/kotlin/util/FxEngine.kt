package it.fraudata.util

import it.fraudata.domain.Currency
import java.math.BigDecimal
import java.math.RoundingMode

object FxEngine {

    // Mappa dei tassi di cambio rispetto all'EUR (Valuta base fittizia)
    private val ratesToEur = mapOf(
        Currency.EUR to BigDecimal("1.00"),
        Currency.USD to BigDecimal("1.08"),
        Currency.GBP to BigDecimal("0.85"),
        Currency.JPY to BigDecimal("160.50"),
        Currency.CHF to BigDecimal("0.97")
    )

    /**
     * Converte un importo da una valuta all'altra.
     */
    fun convert(amount: BigDecimal, from: Currency, to: Currency): BigDecimal {
        if (from == to) return amount

        val rateFrom = ratesToEur[from] ?: throw IllegalArgumentException("Unsupported currency: $from")
        val rateTo = ratesToEur[to] ?: throw IllegalArgumentException("Unsupported currency: $to")

        // Formula: (Amount / RateFrom) * RateTo
        // Usiamo RoundingMode.HALF_UP e scale 4 nei calcoli intermedi per non perdere precisione
        val amountInEur = amount.divide(rateFrom, 4, RoundingMode.HALF_UP)
        val convertedAmount = amountInEur.multiply(rateTo)

        return convertedAmount.setScale(2, RoundingMode.HALF_UP)
    }
}