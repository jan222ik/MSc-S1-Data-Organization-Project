package messaging_api

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoHandler {

    private lateinit var col: String

    init {
        val client = KMongo.createClient().coroutine //use coroutine extension
        val database = client.getDatabase("messagehistory") //normal java driver usage
        //val col = database.getCollection<Message>() //KMongo extension method
        database.getCollection("messages", Message::class.java).coroutine
    }

    suspend fun pushMessage(msg: Message) {
        col.insertOne(Jedi("Luke Skywalker", 19))
        val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")
    }
}
