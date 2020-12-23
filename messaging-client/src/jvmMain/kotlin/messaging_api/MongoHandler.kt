package messaging_api

import kotlinx.coroutines.delay
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.Closeable
import java.time.LocalDateTime

class MongoHandler : Closeable {

    private val client: CoroutineClient = KMongo.createClient().coroutine
    private val col: CoroutineCollection<Message>

    init {
        val database = client.getDatabase("messagehistory") //normal java driver usage
        col = database.getCollection("messages") //KMongo extension method
    }

    suspend fun getHistory(): List<Message> = col.find().toList().sortedBy { it.timestamp }

    suspend fun pushMessage(msg: Message) = col.insertOne(msg)

    override fun close() {
        client.close()
    }

}

suspend fun main() {
    MongoHandler().apply {
        pushMessage(Message("test", LocalDateTime.now(), Author("Janik", "@@@@")))
        delay(2000)
        getHistory().forEach {
            println(it)
        }
    }
}
