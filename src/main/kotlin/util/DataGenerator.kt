package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.CountryCode
import it.fraudata.domain.Transaction
import it.fraudata.domain.TransactionType
import it.fraudata.domain.Currency
import net.datafaker.Faker
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.logging.Logger

class DataGenerator(private val faker: Faker = Faker()) {

    private val logger = KotlinLogging.logger {}

    fun saveAccountToDb(account: Account) = transaction {
        AccountsTable.insert {
            it[id] = account.id
            it[customerName] = account.customerName
            it[balance] = account.balance
            it[countryCode] = account.countryCode.value
            it[baseCurrency] = account.baseCurrency.name
        }
    }
    
    fun saveTransactionToDb(tx: Transaction) = transaction {
        TransactionsTable.insert {
            it[id] = tx.id
            it[accountId] = tx.accountId
            it[amount] = tx.amount
            it[currency] = tx.currency.name
            it[baseAmount] = tx.baseAmount
            it[type] = tx.type.name
            it[timestamp] = tx.timestamp // Assicurati che il formato sia compatibile (LocalDateTime)
            it[merchantName] = tx.merchantName
            it[isFraudulent] = tx.isFraudulent
        }

    fun generateAccounts(count: Int, targetCountryCode: CountryCode): List<Account> {
        logger.info("Generating $count synthetic accounts for country ${targetCountryCode.value}...")
    
        // Decidiamo la valuta base in base al paese (Logica super semplificata)
        val defaultCurrency = when (targetCountryCode.value) {
            "GB" -> Currency.GBP
            "US" -> Currency.USD
            "CH" -> Currency.CHF
            "JP" -> Currency.JPY
            else -> Currency.EUR
        }
    
        return List(count) {
            Account(
                customerName = faker.name().fullName(),
                balance = BigDecimal(faker.commerce().price(10.0, 10000.0)).setScale(2, RoundingMode.HALF_UP),
                countryCode = targetCountryCode,
                baseCurrency = defaultCurrency // Usiamo la valuta calcolata
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
        
        // Start with a salary to give the user some initial funds
        var currentState = it.fraudata.domain.ExpenseCategory.SALARY
        var currentDateTime = LocalDateTime.now().minusDays(30) // Start 30 days ago

        for (i in 0 until count) {
            
            // 1. Find the spend profile for the current category
            val spendProfile = persona.spendProfiles[currentState] 
                ?: throw IllegalStateException("Missing spend profile for $currentState")

            // 2. Generate the correct amount based on the Persona limits
            val amount = BigDecimal(
                faker.number().randomDouble(2, spendProfile.minAmount.toInt(), spendProfile.maxAmount.toInt())
            ).setScale(2, RoundingMode.HALF_UP)

            // 3. Determine if it's a deposit or a withdrawal
            val txType = if (currentState == it.fraudata.domain.ExpenseCategory.SALARY) {
                TransactionType.DEPOSIT
            } else {
                TransactionType.WITHDRAWAL
            }

            // 4. MULTI-CURRENCY LOGIC
            // 15% chance that the user is buying from a foreign website or traveling
            val isForeignTransaction = faker.number().randomDouble(2, 0, 100) <= 15.0
        
            val transactionCurrency = if (isForeignTransaction) {
                // Pick a random currency different from the account's base currency
                Currency.entries.filter { it != account.baseCurrency }.random()
            } else {
                account.baseCurrency
            }
    
            // Calculate the actual cost in the account's currency using our FxEngine
            val baseAmount = FxEngine.convert(
                amount = amount, 
                from = transactionCurrency, 
                to = account.baseCurrency
            )
    
            // 5. Create the transaction (Once, with all the correct fields!)
            transactions.add(
                Transaction(
                    accountId = account.id,
                    amount = amount,
                    currency = transactionCurrency,
                    baseAmount = baseAmount,
                    type = txType,
                    timestamp = currentDateTime,
                    merchantName = if (txType == TransactionType.WITHDRAWAL) "${currentState.name} MERCHANT" else "EMPLOYER INC.",
                    isFraudulent = false
                )
            )

            // 6. Predict the next state using the Extension Function
            currentState = persona.transitionMatrix.getNextState(currentState)
            
            // Advance time randomly for the next transaction
            currentDateTime = currentDateTime.plusHours(faker.number().numberBetween(2L, 48L))
        }

        return transactions
    }
}