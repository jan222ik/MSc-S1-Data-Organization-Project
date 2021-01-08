package messaging_api

import com.google.gson.Gson

/**
 * Entrypoint to Client that pushes messages from Redis message subscriber to MongoDB.
 */
fun main() {
    val redisHandler = LettuceHandler()
    val mongoHandler = MongoHandler()
    val gson = Gson()

    redisHandler.connect { msgJson ->
        val msg = gson.fromJson(msgJson, Message::class.java)
        println("New Message: $msg")
        mongoHandler.pushMessage(msg)
    }
}
