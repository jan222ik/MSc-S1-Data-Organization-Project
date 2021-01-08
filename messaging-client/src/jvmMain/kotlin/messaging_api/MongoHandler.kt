package messaging_api

import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineMapReducePublisher
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.lte
import org.litote.kmongo.match
import org.litote.kmongo.reactivestreams.KMongo
import java.io.Closeable

class MongoHandler : Closeable {

    private val client: CoroutineClient
    private val messageHistoryCol: CoroutineCollection<Message>
    private val senderStatCol: CoroutineCollection<SenderStat>

    init {
        // Create a client with coroutine functionality.
        client = KMongo.createClient().coroutine
        // Get Database with the name messagehistory.
        val database = client.getDatabase("messagehistory")
        // Initialize coroutine collection messages & senderstats
        messageHistoryCol = database.getCollection("messages")
        senderStatCol = database.getCollection("senderstats")
    }

    /**
     * Retrieve Message History asynchronously from collection.
     * @return List of messages sorted by their timestamp ascending
     */
    suspend fun getHistory(): List<Message> = messageHistoryCol.find().toList().sortedBy { it.timestamp }

    /**
     * Filters messages based on the provided filter.
     * If a param of the filter class is null it is ignored in the query.
     */
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

    /**
     * Adds a message to the collection 'messagehistory'.
     * @param msg Message to add
     * @return InsertOneResult
     */
    suspend fun pushMessage(msg: Message) = messageHistoryCol.insertOne(msg)

    /**
     * Triggers recalculation and repopulation of the 'senderstats' collection.
     */
    suspend fun calculateSenderStats(): List<SenderStat> {
        // Drop collection to delete old results
        senderStatCol.drop()
        // MapReduce Call
        val mapReduce: CoroutineMapReducePublisher<SenderStat> = messageHistoryCol.mapReduce(
            mapFunction = """
                function() {
                    emit(this.author, 1);
                };
            """,
            reduceFunction = """
                function(author, contents) {
                    return contents.length;
                };
            """
        )
        // Inset Results into 'senderstats' collections
        senderStatCol.insertMany(
            documents = mapReduce.toList()
        )
        // Query all SenderStat from newly populated collection
        return senderStatCol.find().toList()
    }

    /**
     * Drops the 'messagehistory' collection.
     */
    suspend fun dropMessageHistory() {
        messageHistoryCol.drop()
    }


    override fun close() {
        client.close()
    }

}

/**
 * Entrypoint to drop message history collection.
 */
suspend fun main() {
    MongoHandler().dropMessageHistory()
}
