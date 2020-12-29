package messaging_api

import com.google.gson.Gson

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
