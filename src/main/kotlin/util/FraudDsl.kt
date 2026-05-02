package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.CountryCode
import it.fraudata.domain.Transaction
import kotlin.error
import io.github.oshai.kotlinlogging.KotlinLogging

@DslMarker
annotation class FraudGenDsl

data class AccountsConfig(var count : Int=10, var countryCode: String="US")
data class TransactionsConfig(var countPerAccount: Int = 5, var personaName: String = "Salaryman")

@FraudGenDsl
class AccountsBuilder{
    var count : Int =10
    var countryCode : String="US"

    fun build() = AccountsConfig(count,countryCode)
}

@FraudGenDsl
class TransactionsBuilder {
    var countPerAccount: Int = 5
    var persona: String = "Salaryman"
    fun build() = TransactionsConfig(countPerAccount, persona)
}

@FraudGenDsl
class FraudDataBuilder {
    private var accountsConfig = AccountsConfig()
    private var transactionsConfig = TransactionsConfig()
    private val logger = KotlinLogging.logger {}


    fun accounts(block: AccountsBuilder.() -> Unit) {
        val builder = AccountsBuilder()
        builder.block()
        accountsConfig = builder.build()
    }

    fun transactions(block: TransactionsBuilder.() -> Unit) {
        val builder = TransactionsBuilder()
        builder.block()
        transactionsConfig = builder.build()
    }

    fun build(): Pair<List<Account>, List<Transaction>> {
        logger.info { "Initializing dataset generation with behavioral engine..." }
        
        val generator = DataGenerator() 
        val safeCountryCode = CountryCode(accountsConfig.countryCode)
        
        // 1. Generiamo i conti (in memoria)
        val generatedAccounts = generator.generateAccounts(
            count = accountsConfig.count,
            targetCountryCode = safeCountryCode
        )
        
        // 2. Mappiamo la stringa passata dall'utente all'oggetto Persona reale
        val selectedPersona = when (transactionsConfig.personaName.lowercase()) {
            "student" -> it.fraudata.domain.Student
            "gambler" -> it.fraudata.domain.Gambler
            else -> it.fraudata.domain.Salaryman 
        }
        
        // 3. Generiamo le transazioni comportamentali (in memoria)
        val allTransactions = mutableListOf<Transaction>()
        for (account in generatedAccounts) {
            val accountTransactions = generator.generateBehavioralTransactions(
                account = account,
                count = transactionsConfig.countPerAccount,
                persona = selectedPersona 
            )
            allTransactions.addAll(accountTransactions)
        }

        // ==========================================
        // 4. PERSISTENZA SU DATABASE
        // ==========================================
        logger.info { "Saving ${generatedAccounts.size} accounts and ${allTransactions.size} transactions to PostgreSQL..." }
        
        try {
            // Iteriamo e salviamo usando le funzioni che hai creato nello step precedente
            // (Assicurati che saveAccountToDb e saveTransactionToDb siano accessibili qui,
            // ad esempio importandole se le hai messe in un altro file o chiamandole su generator)
            generatedAccounts.forEach { generator.saveAccountToDb(it) }
            allTransactions.forEach { generator.saveTransactionToDb(it) }
            
            logger.info { "Database persistence completed successfully." }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save generated data to the database!" }
            // Decidi se vuoi fermare tutto lanciando un'eccezione o continuare e restituire i dati generati
            // throw e 
        }
        
        return Pair(generatedAccounts, allTransactions)
    }
}

// 3. L'Entry Point del nostro DSL (La funzione globale)
fun generateFraudData(block: FraudDataBuilder.() -> Unit): Pair<List<Account>, List<Transaction>> {
    val builder = FraudDataBuilder()
    builder.block()
    return builder.build()
}