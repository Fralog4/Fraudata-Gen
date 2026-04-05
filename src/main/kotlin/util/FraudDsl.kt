package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.CountryCode
import it.fraudata.domain.Transaction

@DslMarker
annotation class FrauDataDsl

data class AccountsConfig(var count : Int=10, var countryCode: String="US")
data class TransactionsConfig(var countPerAccount: Int = 5, var personaName: String = "Salaryman")

@FrauDataDsl
class AccountBuilder{
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
        println("Initializing dataset generation with behavioral engine...")
        
        val generator = DataGenerator() 
        val safeCountryCode = CountryCode(accountsConfig.countryCode)
        
        // 1. Generiamo i conti
        val generatedAccounts = generator.generateAccounts(
            count = accountsConfig.count,
            targetCountryCode = safeCountryCode
        )
        
        // 2. KOTLIN MAGIC: L'espressione "when"
        // Mappiamo la stringa passata dall'utente all'oggetto Persona reale
        val selectedPersona = when (transactionsConfig.personaName.lowercase()) {
            "student" -> it.fraudata.domain.Student
            "gambler" -> it.fraudata.domain.Gambler
            else -> it.fraudata.domain.Salaryman // Fallback di default sicuro
        }
        
        // 3. Generiamo le transazioni comportamentali
        val allTransactions = mutableListOf<Transaction>()
        for (account in generatedAccounts) {
            val accountTransactions = generator.generateBehavioralTransactions(
                account = account,
                count = transactionsConfig.countPerAccount,
                persona = selectedPersona // Passiamo l'oggetto Persona!
            )
            allTransactions.addAll(accountTransactions)
        }
        
        return Pair(generatedAccounts, allTransactions)
    }
}

// 3. L'Entry Point del nostro DSL (La funzione globale)
fun generateFraudData(block: FrauDataBuilder.() -> Unit): Pair<List<Account>, List<Transaction>> {
    val builder = FrauDataBuilder()
    builder.block()
    return builder.build()
}