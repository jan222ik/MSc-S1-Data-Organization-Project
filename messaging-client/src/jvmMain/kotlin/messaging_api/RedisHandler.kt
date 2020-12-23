package messaging_api

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.io.Closeable
import java.time.LocalDateTime

object RedisHandler : Closeable {

    lateinit var jedis: Jedis
    lateinit var pubSub: PubSub

    fun connect(onNewMessage: suspend (Message) -> Unit) {
        jedis = Jedis("localhost", 6379)
        jedis.auth( "redis")
        jedis.connect()

        pubSub = PubSub(
            onNewMessage = onNewMessage
        )

        val job = GlobalScope.launch {
            pubSub.proceed(jedis.client, "some")
        }

    }

    override fun close() {
        jedis.disconnect()
    }

    fun writeMessage(msg: Message) {
        jedis.publish("some", msg.content)
    }

    class PubSub(private val onNewMessage: suspend (Message) -> Unit) : JedisPubSub() {

        override fun onMessage(channel: String?, message: String?) {
            super.onMessage(channel, message)
            GlobalScope.launch {
                onNewMessage(Message(content = message ?: "", LocalDateTime.now(), Author("", "")))
            }
        }


    }
}
