package messaging_api

import com.google.gson.Gson
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reactor.core.Disposable
import java.io.Closeable
import java.time.LocalDateTime

/**
 * Constant for Redis Channel name
 */
private const val CHANNEL = "some"

class LettuceHandler : Closeable {
    private var disposable: Disposable? = null
    private lateinit var subClient: RedisClient
    private lateinit var pubClient: RedisClient


    /**
     * Establishes a connection to the redis database and connects a subscriber and a publisher.
     * A separate pubClient is used to maintain a hot subscriber at all time.
     * @param onNextMessage a asynchronous lambda is executed when a new message is published.
     */
    fun connect(onNextMessage: suspend (String) -> Unit) {
        subClient = RedisClient.create("redis://redis@localhost")
        pubClient = RedisClient.create("redis://redis@localhost")

        // Offers RedisPubSub commands for a client's connection.
        val reactive: RedisPubSubReactiveCommands<String, String> = subClient.connectPubSub().reactive()

        // Subscribe to the channel
        reactive.subscribe(CHANNEL).subscribe()

        // Observe channels the reactive commands are subscribed to.
        // This operation blocks the subscriber.
        disposable = reactive.observeChannels().doOnNext { msg ->
            GlobalScope.launch {
                onNextMessage.invoke(msg.message)
            }
        }.subscribe()
    }

    override fun close() {
        // Do clean shutdown
        disposable?.dispose()
        subClient.shutdown()
        pubClient.shutdown()
    }


    /**
     * Send a message with Redis Publisher.
     * The message is stringified to JSON.
     * @param msg Message obj
     */
    fun sendMessage(msg: Message) {
        val gson = Gson()
        val jsonString = gson.toJson(msg)
        // Open Connection to Channel and publish jsonString.
        pubClient.connectPubSub().reactive().publish(CHANNEL, jsonString).block()
    }


}

/**
 * Simple Program to observe the channel and subsequently to send a message into that channel.
 */
fun main() {
    val lettuceHandler = LettuceHandler()
    lettuceHandler.connect {
        println("it = $it")
    }
    lettuceHandler.sendMessage(Message(content = "Pizza", LocalDateTime.now(), Author("Green Guy", "@")))
}


