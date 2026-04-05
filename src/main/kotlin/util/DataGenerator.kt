package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.CountryCode
import it.fraudata.domain.Transaction
import it.fraudata.domain.TransactionType
import net.datafaker.Faker
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.logging.Logger

class DataGenerator(private val faker: Faker = Faker()) {

    private val logger = Logger.getLogger(DataGenerator::class.java.name)

    fun generateAccounts(count: Int, targetCountryCode: CountryCode): List<Account> {
        logger.info("Generating $count synthetic accounts for country ${targetCountryCode.value}...")

        return List(count) {

            Account(
                customerName = faker.name().fullName(),
                balance = BigDecimal(faker.commerce().price(10.0, 10000.0)).setScale(2, RoundingMode.HALF_UP),
                countryCode = targetCountryCode
            )

        }
    }

    fun generateTransForAccount(account: Account, count: Int,fraudProbability : Double): List<Transaction> {
        logger.info("Generating $count transactions for account ${account.id}...")
        return List(count) {

            val isWithdrawal = faker.bool().bool()
            val isFraud = faker.number().randomDouble(2, 0, 100) <= fraudProbability

            Transaction(
                accountId = account.id,
                amount = BigDecimal(faker.commerce().price(1.0, 1000.0)).setScale(2, RoundingMode.HALF_UP),
                type = if (isWithdrawal) TransactionType.WITHDRAWAL else TransactionType.DEPOSIT,
                timestamp = LocalDateTime.now().minusDays(faker.number().numberBetween(1L, 30L)),
                merchantName = if (isWithdrawal) faker.company().name() else null,
                isFraudulent = isFraud            )
        }
    }
}