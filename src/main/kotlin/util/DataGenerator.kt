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

    fun generateBehavioralTransactions(
        account: Account, 
        count: Int, 
        persona: it.fraudata.domain.Persona
    ): List<Transaction> {
        
        logger.info("Generating $count behavioral transactions for account ${account.id} matching persona: ${persona.name}")
        
        val transactions = mutableListOf<Transaction>()
        
        // Partiamo con uno stipendio per dare fondi all'utente
        var currentState = it.fraudata.domain.ExpenseCategory.SALARY
        var currentDateTime = LocalDateTime.now().minusDays(30) // Partiamo da 30 giorni fa

        for (i in 0 until count) {
            // 1. Troviamo il profilo di spesa per la categoria corrente
            val spendProfile = persona.spendProfiles[currentState] 
                ?: throw IllegalStateException("Missing spend profile for $currentState")

            // 2. Generiamo l'importo corretto in base ai limiti della Persona
            val amount = BigDecimal(
                faker.number().randomDouble(2, spendProfile.minAmount.toInt(), spendProfile.maxAmount.toInt())
            ).setScale(2, RoundingMode.HALF_UP)

            // 3. Capiamo se è un deposito o un prelievo
            val txType = if (currentState == it.fraudata.domain.ExpenseCategory.SALARY) {
                TransactionType.DEPOSIT
            } else {
                TransactionType.WITHDRAWAL
            }

            // 4. Creiamo la transazione
            transactions.add(
                Transaction(
                    accountId = account.id,
                    amount = amount,
                    type = txType,
                    timestamp = currentDateTime,
                    merchantName = if (txType == TransactionType.WITHDRAWAL) "${currentState.name} MERCHANT" else "EMPLOYER INC.",
                    isFraudulent = false // Le frodi le gestiremo a parte o possiamo considerarle anomalie
                )
            )

            // 5. Extension Function
            currentState = persona.transitionMatrix.getNextState(currentState)
            
            // Avanziamo il tempo di qualche ora casuale per la prossima transazione
            currentDateTime = currentDateTime.plusHours(faker.number().numberBetween(2L, 48L))
        }

        return transactions
    }
}