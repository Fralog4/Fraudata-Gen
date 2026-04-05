package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.CountryCode
import it.fraudata.domain.Transaction

@DslMarker
annotation class FrauDataDsl

data class AccountsConfig(var count : Int=10, var countryCode: String="US")
data class TransactionConfig(var countPerAccount: Int=5, var fraudProbability : Double=1.0)

@FrauDataDsl
class AccountBuilder{
    var count : Int =10
    var countryCode : String="US"

    fun build() = AccountsConfig(count,countryCode)
}

@FrauDataDsl
class TransactionsBuilder {
    var countPerAccount: Int = 5
    var fraudProbabilityPercentage: Double = 1.0

    fun build() = TransactionConfig(countPerAccount, fraudProbabilityPercentage)
}

@FrauDataDsl
class FrauDataBuilder {
    private var accountsConfig= AccountsConfig()
    private var transactionsConfig = TransactionConfig()

    fun accounts(block: AccountBuilder.()-> Unit){
        val builder = AccountBuilder()
        builder.block()
        accountsConfig=builder.build()
    }

    fun transactions(block: TransactionsBuilder.()-> Unit){

        val builder = TransactionsBuilder()
        builder.block()
        transactionsConfig = builder.build()
    }

    fun build(): Pair<List<Account>, List<Transaction>> {
        println("Initializing dataset generation...")

        val generator = DataGenerator()
        val safeCountryCode = CountryCode(accountsConfig.countryCode)

        val generatedAccounts = generator.generateAccounts(count = accountsConfig.count,
            targetCountryCode = safeCountryCode)

        val allTransactions =  mutableListOf<Transaction>()

        for (account in generatedAccounts ){
            val accountTransaction = generator.generateTransForAccount(account=account,
                count = transactionsConfig.countPerAccount,
                fraudProbability = transactionsConfig.fraudProbability)
            allTransactions.addAll(accountTransaction)
        }

        println("Successfully generated ${generatedAccounts.size} accounts and ${allTransactions.size} transactions.")
        return Pair(generatedAccounts, allTransactions)
    }

}
// 3. L'Entry Point del nostro DSL (La funzione globale)
fun generateFraudData(block: FrauDataBuilder.() -> Unit): Pair<List<Account>, List<Transaction>> {
    val builder = FrauDataBuilder()
    builder.block()
    return builder.build()
}