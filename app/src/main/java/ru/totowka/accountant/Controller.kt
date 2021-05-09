package ru.totowka.accountant

import android.content.Intent
import ru.totowka.accountant.data.AutorizationException
import ru.totowka.accountant.data.TimeFilter
import ru.totowka.accountant.data.extension.toLocalDate
import ru.totowka.accountant.data.model.AuthRepository
import ru.totowka.accountant.data.model.FirestoreRepository
import ru.totowka.accountant.data.model.TransactionScannerStub
import ru.totowka.accountant.data.type.ChartTransactionState
import ru.totowka.accountant.data.type.Transaction

class Controller(
    private val fs: FirestoreRepository = FirestoreRepository(),
    private val auth: AuthRepository = AuthRepository(),
    private val scanner: TransactionScannerStub = TransactionScannerStub()
) {

    fun addUser() {
        auth.getUserID()?.let { fs.addUser(it) }
            ?: throw AutorizationException(AUTH_ERROR)
    }

    fun getAuthIntent(): Intent {
        return auth.getAuthIntent()
    }

    fun isAuthorized(): Boolean = auth.isAuthorized()

    suspend fun getTransactions(filter: TimeFilter): List<Transaction> {
        return auth.getUserID()?.let { fs.getTransactions(it, filter) }
            ?: throw AutorizationException(AUTH_ERROR)
    }

    suspend fun getChartTransactionStates(filter: TimeFilter): List<ChartTransactionState> {
        val transactions = auth.getUserID()?.let { fs.getTransactions(it, filter) }
            ?: throw AutorizationException(AUTH_ERROR)

        return transactions.groupBy { it.date.toLocalDate() }.values.map { transaction ->
            ChartTransactionState(transaction.map { it.total }.sum(), transaction[0].date.seconds)
        }
    }

    fun addTransaction(transaction: Transaction) {
        auth.getUserID()?.let { fs.addTransaction(transaction, it) }
            ?: throw AutorizationException(AUTH_ERROR)
    }

    fun scanQR(qr: String): Transaction {
        return scanner.getTransactionInfo(qr)
    }

    companion object {
        const val AUTH_ERROR = "The user isn't authorized"
    }
}