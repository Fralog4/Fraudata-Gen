package it.fraudata.domain

enum class ExpenseCategory {
    SALARY, RENT, GROCERIES, ENTERTAINMENT, GAMBLING, UTILITIES
}

typealias TransitionMatrix = Map<ExpenseCategory, Map<ExpenseCategory, Double>>

// New Data Class to hold the minimum and maximum amount for a specific category
data class SpendProfile(val minAmount: Double, val maxAmount: Double)

sealed interface Persona {
    val name: String
    val transitionMatrix: TransitionMatrix
    val spendProfiles: Map<ExpenseCategory, SpendProfile>
}

data object Salaryman : Persona {
    override val name = "The Salaryman"
    
    // Stable income, moderate expenses
    override val spendProfiles = mapOf(
        ExpenseCategory.SALARY to SpendProfile(2500.0, 3500.0),
        ExpenseCategory.RENT to SpendProfile(800.0, 1200.0),
        ExpenseCategory.GROCERIES to SpendProfile(40.0, 150.0),
        ExpenseCategory.ENTERTAINMENT to SpendProfile(20.0, 80.0),
        ExpenseCategory.GAMBLING to SpendProfile(0.0, 0.0),
        ExpenseCategory.UTILITIES to SpendProfile(80.0, 200.0)
    )

    override val transitionMatrix: TransitionMatrix = mapOf(
        ExpenseCategory.SALARY to mapOf(
            ExpenseCategory.RENT to 0.5,
            ExpenseCategory.UTILITIES to 0.3,
            ExpenseCategory.GROCERIES to 0.2
        ),
        ExpenseCategory.RENT to mapOf(
            ExpenseCategory.GROCERIES to 0.6,
            ExpenseCategory.ENTERTAINMENT to 0.4
        ),
        ExpenseCategory.GROCERIES to mapOf(
            ExpenseCategory.GROCERIES to 0.4, // Buys groceries again
            ExpenseCategory.ENTERTAINMENT to 0.5,
            ExpenseCategory.UTILITIES to 0.1
        ),
        ExpenseCategory.ENTERTAINMENT to mapOf(
            ExpenseCategory.GROCERIES to 0.7,
            ExpenseCategory.ENTERTAINMENT to 0.2,
            ExpenseCategory.SALARY to 0.1 // Month loops back
        ),
        ExpenseCategory.UTILITIES to mapOf(
            ExpenseCategory.GROCERIES to 0.8,
            ExpenseCategory.ENTERTAINMENT to 0.2
        ),
        ExpenseCategory.GAMBLING to mapOf(
            ExpenseCategory.GROCERIES to 1.0 // If he ever gambles, he stops immediately
        )
    )
}

data object Student : Persona {
    override val name = "The Student"
    
    // Low income (allowance), high entertainment spending
    override val spendProfiles = mapOf(
        ExpenseCategory.SALARY to SpendProfile(400.0, 800.0),
        ExpenseCategory.RENT to SpendProfile(300.0, 500.0),
        ExpenseCategory.GROCERIES to SpendProfile(10.0, 40.0),
        ExpenseCategory.ENTERTAINMENT to SpendProfile(30.0, 150.0),
        ExpenseCategory.GAMBLING to SpendProfile(5.0, 20.0),
        ExpenseCategory.UTILITIES to SpendProfile(30.0, 80.0)
    )

    override val transitionMatrix: TransitionMatrix = mapOf(
        ExpenseCategory.SALARY to mapOf(
            ExpenseCategory.RENT to 0.4,
            ExpenseCategory.ENTERTAINMENT to 0.4,
            ExpenseCategory.GROCERIES to 0.2
        ),
        ExpenseCategory.RENT to mapOf(
            ExpenseCategory.ENTERTAINMENT to 0.6,
            ExpenseCategory.GROCERIES to 0.4
        ),
        ExpenseCategory.GROCERIES to mapOf(
            ExpenseCategory.ENTERTAINMENT to 0.7,
            ExpenseCategory.GROCERIES to 0.3
        ),
        ExpenseCategory.ENTERTAINMENT to mapOf(
            ExpenseCategory.ENTERTAINMENT to 0.5, // The student parties back-to-back!
            ExpenseCategory.GROCERIES to 0.3,
            ExpenseCategory.GAMBLING to 0.1,
            ExpenseCategory.SALARY to 0.1 
        ),
        ExpenseCategory.GAMBLING to mapOf(
            ExpenseCategory.ENTERTAINMENT to 0.5,
            ExpenseCategory.GROCERIES to 0.5
        ),
        ExpenseCategory.UTILITIES to mapOf(
            ExpenseCategory.GROCERIES to 1.0
        )
    )
}

data object Gambler : Persona {
    override val name = "The Gambler"
    
    // Unpredictable income, massive gambling expenses
    override val spendProfiles = mapOf(
        ExpenseCategory.SALARY to SpendProfile(1000.0, 5000.0),
        ExpenseCategory.RENT to SpendProfile(500.0, 1000.0),
        ExpenseCategory.GROCERIES to SpendProfile(20.0, 60.0),
        ExpenseCategory.ENTERTAINMENT to SpendProfile(50.0, 200.0),
        ExpenseCategory.GAMBLING to SpendProfile(100.0, 2000.0),
        ExpenseCategory.UTILITIES to SpendProfile(50.0, 150.0)
    )

    override val transitionMatrix: TransitionMatrix = mapOf(
        ExpenseCategory.SALARY to mapOf(
            ExpenseCategory.GAMBLING to 0.8, // Immediately gambles the salary
            ExpenseCategory.RENT to 0.1,
            ExpenseCategory.GROCERIES to 0.1
        ),
        ExpenseCategory.RENT to mapOf(
            ExpenseCategory.GAMBLING to 0.9,
            ExpenseCategory.GROCERIES to 0.1
        ),
        ExpenseCategory.GROCERIES to mapOf(
            ExpenseCategory.GAMBLING to 0.8,
            ExpenseCategory.ENTERTAINMENT to 0.2
        ),
        ExpenseCategory.ENTERTAINMENT to mapOf(
            ExpenseCategory.GAMBLING to 1.0
        ),
        ExpenseCategory.GAMBLING to mapOf(
            ExpenseCategory.GAMBLING to 0.7, // Addictive behavior loop
            ExpenseCategory.ENTERTAINMENT to 0.1,
            ExpenseCategory.GROCERIES to 0.1,
            ExpenseCategory.SALARY to 0.1
        ),
        ExpenseCategory.UTILITIES to mapOf(
            ExpenseCategory.GAMBLING to 1.0
        )
    )
}