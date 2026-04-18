package it.fraudata.util

import it.fraudata.domain.Transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import kotlinx.coroutines.delay

object KafkaExporter {

    private val producer : KafkaProducer<String,String>
    private const val TOPIC = "transactions"
    private val jsonFormatter = Json {prettyPrint = false}


init{
    println("Initializing Kafka Producer...")
        val props = Properties()
        // Dove si trova il server Kafka
        props["bootstrap.servers"] = "localhost:9092"

        // Diciamo a Kafka come trasformare i nostri dati in byte (usiamo StringSerializer per entrambi)
        props["key.serializer"] = StringSerializer::class.java.name
        props["value.serializer"] = StringSerializer::class.java.name

        props["acks"]=1 // acks=1 significa "non bloccarti aspettando che tutti i server confermino, basta il primo"
        producer = KafkaProducer<String,String>(props)

}

fun streamTransactions(transactions : List<Transaction>){

    println("Streaming ${transactions.size} transactions to Kafka topic '$TOPIC'...")


    for (tx in transactions){
        val jsonValue = jsonFormatter.encodeToString(tx)

        /**
         * I topic Kafka sono divisi in Partizioni per scalare su più server.
         *  Se non metti una Key, Kafka distribuisce i messaggi a caso (Round-robin).
         *  Ma se metti l'accountId come Key, Kafka garantisce matematicamente che tutte le transazioni di un utente finiranno 
         * sempre nella stessa partizione.
         *  Questo significa che i sistemi di antifrode le leggeranno nell'ordine temporale esatto in cui sono avvenute, 
         * prevenendo falsi positivi.
         */
        val record = ProducerRecord(TOPIC,tx.accountId, jsonValue)

        producer.send(record) // send() in Kafka è asincrono. Mette il messaggio in una coda interna e non blocca il codice
    }

    producer.flush()
    println("Kafka streaming completed.")
}

/**
     * Invia le transazioni a Kafka simulando un traffico in tempo reale.
     * La keyword 'suspend' ci permette di usare 'delay()' senza bloccare il server.
     */
    suspend fun streamTransactionsLive (transactions : List<Transaction>, tps: Int){

        println("Starting LIVE stream of ${transactions.size} transactions at $tps TPS...")

        val delayMillis = 1000L / tps.coerceAtLeast(1) // Evitiamo divisione per zero


        for (tx in transactions){

            val jsonValue = jsonFormatter.encodeToString(tx)
            val record = ProducerRecord (TOPIC, tx.accountId, jsonValue)
            producer.send(record)
            //Kotlin coroutines ci permette di "dormire" senza bloccare il thread, quindi il server può continuare a rispondere ad altre richieste
            delay(delayMillis)
        }

        producer.flush()
        println("LIVE Kafka streaming completed.")

    }
}