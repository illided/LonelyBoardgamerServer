package actions.commands

import com.twoilya.lonelyboardgamer.WithCodeException
import com.twoilya.lonelyboardgamer.tables.dbQuery
import io.ktor.http.*

abstract class TableCommand<T> {
    suspend fun run(userId: String, parameters: Parameters = Parameters.Empty): T {
        return try {
            dbQuery { query(userId, parameters) }
        } catch (exception: Exception) {
            if (exception !is WithCodeException) {
                writeLog(parameters)
            }
            throw exception
        }
    }

    protected abstract fun query(userId: String, parameters: Parameters = Parameters.Empty): T

    protected val logMessage: String = "No message provided"

    private fun writeLog(params: Parameters) {

    }
}