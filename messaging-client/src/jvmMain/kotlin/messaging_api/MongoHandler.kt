package messaging_api

import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.lte
import org.litote.kmongo.match
import org.litote.kmongo.reactivestreams.KMongo
import java.io.Closeable

class MongoHandler : Closeable {

    private val client: CoroutineClient = KMongo.createClient().coroutine
    private val messageHistoryCol: CoroutineCollection<Message>

    init {
        val database = client.getDatabase("messagehistory")
        messageHistoryCol = database.getCollection("messages")
    }

    suspend fun getHistory(): List<Message> = messageHistoryCol.find().toList().sortedBy { it.timestamp }

    suspend fun filterMessages(filter: MessageFilter?): List<Message> {
        val pipeline = mutableListOf<Bson>()
        filter?.apply {
            var query: Bson? = null
            author?.email?.let {
                query = Message::author / Author::email eq it
            }
            startDateTime?.let {
                val laterThen = Message::timestamp gte it
                query = laterThen.takeUnless { query != null } ?: and(query, laterThen)
            }
            endDateTime?.let {
                val beforeThat = Message::timestamp lte it
                query = beforeThat.takeUnless { query != null } ?: and(query, beforeThat)
            }
            query?.let {
                pipeline.add(match(it))
            }
        }
        return messageHistoryCol.aggregate<Message>(pipeline).toList()
    }

    suspend fun pushMessage(msg: Message) = messageHistoryCol.insertOne(msg)

    suspend fun calculateSenderStats(): List<SenderStat> {
        return messageHistoryCol.mapReduce<SenderStat>(
            """
                function() {
                    emit(this.author, 1);
                };
            """,
            """
                function(author, contents) {
                    return contents.length;
                };
            """
        ).toList()
    }


    override fun close() {
        client.close()
    }

}

suspend fun main() {
    MongoHandler().apply {

        //pushMessage(Message("test", LocalDateTime.now(), Author("Janik", "@@@@")))
        //delay(2000)
        //getHistory().forEach {
        //    println(it)
        //}

        calculateSenderStats().forEach {
            println(it)
        }
    }
}
