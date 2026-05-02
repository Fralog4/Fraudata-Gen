package it.fraudata.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

// Definizione dello Schema (Tabelle)
object AccountsTable : Table("accounts") {

    val id = varchar("id",50)
    val costumerName = varchar("customer_name", 255)
    val balance = decimal("balance",15,2)
    val countryCode = varchar("country_code",2)
    val baseCurrency = varchar("base_currency",3)

    override val primaryKey = PrimaryKey(id)
}
object TransactionsTable : Table("transactions") {
    val id = varchar("id", 50)
    val accountId = varchar("account_id", 50) references AccountsTable.id
    val amount = decimal("amount", 15, 2)
    val currency = varchar("currency", 3)
    val baseAmount = decimal("base_amount", 15, 2)
    val type = varchar("type", 20)
    val timestamp = datetime("timestamp")
    val merchantName = varchar("merchant_name", 255).nullable()
    val isFraudulent = bool("is_fraudulent")

    override val primaryKey = PrimaryKey(id)
}
object DatabaseFactory {

    fun init(config: io.ktor.server.config.ApplicationConfig) {
        val driverClassName = config.property("fraudgen.storage.driver").getString()
        val jdbcUrl = config.property("fraudgen.storage.jdbcUrl").getString()
        val user = config.property("fraudgen.storage.user").getString()
        val password = config.property("fraudgen.storage.password").getString()


        val connectionPool = HikariDataSource(HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            maximumPoolSize = 50
            isAutoCommit = false

            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()

        })

        Database.connect(connectionPool)

        //Auto create of Tables
        transaction {
            SchemaUtils.create(AccountsTable, TransactionsTable)
        }

    }
}