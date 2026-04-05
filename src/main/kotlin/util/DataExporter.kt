package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.Transaction
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

object DataExporter {
    private val jsonFormatter = Json { prettyPrint = true }


    fun exportToJson(accounts: List<Account>, transactions: List<Transaction>) {
        println("Exporting data to JSON...")

        val accountsJson = jsonFormatter.encodeToString(accounts)
        val transactionsJson = jsonFormatter.encodeToString(transactions)

        File("accounts.json").writeText(accountsJson)
        File("transactions.json").writeText(transactionsJson)

        println("JSON export completed successfully.")

    }


    fun exportToCsv(accounts: List<Account>, transactions: List<Transaction>) {
        println("Exporting data to CSV...")

        val accountHeader = "id,customerName,balance,countryCode"

        // Kotlin Magic: joinToString replaces complex loops and StringBuilders
        val accountsCsv = accounts.joinToString(separator = "\n", prefix = "$accountHeader\n") {
            "${it.id},\"${it.customerName}\",${it.balance},${it.countryCode.value}"
        }

        val txHeader = "id,accountId,amount,type,timestamp,merchantName,isFraudulent"
        val transactionsCsv = transactions.joinToString(separator = "\n", prefix = "$txHeader\n") {
            // Handle null merchant name gracefully
            val merchant = it.merchantName?.let { name -> "\"$name\"" } ?: ""
            "${it.id},${it.accountId},${it.amount},${it.type},${it.timestamp},$merchant,${it.isFraudulent}"
        }

        File("accounts.csv").writeText(accountsCsv)
        File("transactions.csv").writeText(transactionsCsv)

        println("CSV export completed successfully.")

    }
}