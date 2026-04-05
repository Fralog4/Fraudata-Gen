package it.fraudata.util

import it.fraudata.domain.ExpenseCategory
import it.fraudata.domain.TransitionMatrix
import kotlin.random.Random

/**
 * Extension function to calculate the next state based on Markov Chain probabilities.
 */
 fun TransitionMatrix.getNextState(currentState : ExpenseCategory):ExpenseCategory {


    //find the probabilities for the current state
    val probabilities = this[currentState] ?: throw IllegalStateException("Critical Error: State $currentState has no outward transitions defined!")

    //random number between 0.0 and 0.1

    val randomValue = Random.nextDouble()
    var cumulativeProbability = 0.0

    for ((nextState,probability) in probabilities){

        cumulativeProbability += probability

        if(randomValue <= cumulativeProbability){return nextState}
    }

    // 4. Fallback di sicurezza
    // A causa di imprecisioni matematiche della CPU sui numeri decimali (Double), 
    // la somma potrebbe fare 0.999999999 anziché 1.0. 
    // Se il ciclo finisce senza ritorni, restituiamo forzatamente l'ultimo stato della mappa.
    return probabilities.keys.last() 
 }