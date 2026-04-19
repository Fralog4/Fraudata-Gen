package it.fraudata.util

import it.fraudata.domain.Account
import it.fraudata.domain.Transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object DataExporter {
    
    private val jsonFormatter = Json { prettyPrint = true }
    private val logger = KotlinLogging.logger {}

    fun exportToJson(accounts: List<Account>, transactions: List<Transaction>) {
        logger.info("Exporting data to JSON...")

        val accountsJson = jsonFormatter.encodeToString(accounts)
        val transactionsJson = jsonFormatter.encodeToString(transactions)

        File("accounts.json").writeText(accountsJson)
        File("transactions.json").writeText(transactionsJson)

        logger.info("JSON export completed successfully.")
    }

    fun exportToCsv(accounts: List<Account>, transactions: List<Transaction>) {
        logger.info("Exporting data to CSV...")

        // AGGIORNATO: Aggiunto baseCurrency
        val accountHeader = "id,customerName,balance,countryCode,baseCurrency"
        val accountsCsv = accounts.joinToString(separator = "\n", prefix = "$accountHeader\n") {
            "${it.id},\"${it.customerName}\",${it.balance},${it.countryCode.value},${it.baseCurrency}"
        }

        // AGGIORNATO: Aggiunti currency e baseAmount
        val txHeader = "id,accountId,amount,currency,baseAmount,type,timestamp,merchantName,isFraudulent"
        val transactionsCsv = transactions.joinToString(separator = "\n", prefix = "$txHeader\n") {
            // Handle null merchant name gracefully
            val merchant = it.merchantName?.let { name -> "\"$name\"" } ?: ""
            "${it.id},${it.accountId},${it.amount},${it.currency},${it.baseAmount},${it.type},${it.timestamp},$merchant,${it.isFraudulent}"
        }

        File("accounts.csv").writeText(accountsCsv)
        File("transactions.csv").writeText(transactionsCsv)

        logger.info("CSV export completed successfully.")
    }
}