package messaging_api

import com.google.gson.Gson
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reactor.core.Disposable
import java.io.Closeable
import java.time.LocalDateTime

private const val CHANNEL = "some"

class LettuceHandler : Closeable {
    private var disposable: Disposable? = null
    private lateinit var subClient: RedisClient
    private lateinit var pubClient: RedisClient


    fun connect(onNextMessage: suspend (String) -> Unit) {
        subClient = RedisClient.create("redis://redis@localhost")
        pubClient = RedisClient.create("redis://redis@localhost")

        val connection: StatefulRedisPubSubConnection<String, String> = subClient.connectPubSub()
        val reactive: RedisPubSubReactiveCommands<String, String> = connection.reactive()

        reactive.subscribe(CHANNEL).subscribe()

        disposable = reactive.observeChannels().doOnNext { msg ->
            GlobalScope.launch {
                onNextMessage.invoke(msg.message)
            }
        }.subscribe()
    }

    override fun close() {
        disposable?.dispose()
        subClient.shutdown()
        pubClient.shutdown()
    }


    fun sendMessage(msg: Message) {
        val gson = Gson()
        val jsonString = gson.toJson(msg)
        pubClient.connectPubSub().reactive().publish(CHANNEL, jsonString).block()
    }


}

fun main() {
    val lettuceHandler = LettuceHandler()
    lettuceHandler.connect {
        println("it = $it")
    }
    lettuceHandler.sendMessage(Message(content = "Pizza", LocalDateTime.now(), Author("Green Guy", "@")))
}


