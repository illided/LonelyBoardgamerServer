package actions.commands

import com.twoilya.lonelyboardgamer.WithCodeException
import com.twoilya.lonelyboardgamer.tables.dbQuery
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

abstract class TableCommand<T> {
    suspend fun run(userId: Long? = null, parameters: Parameters = Parameters.Empty): T {
        return try {
            dbQuery { query(userId, parameters) }
        } catch (exception: Exception) {
            if (exception !is WithCodeException) {
                writeLog(parameters)
            }
            throw exception
        }
    }

    protected abstract fun query(userId: Long?, parameters: Parameters): T

    protected val logMessage: String = "No message provided"

    private fun writeLog(params: Parameters) {

    }
}