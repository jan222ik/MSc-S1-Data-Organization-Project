package messaging_api

import kotlinx.coroutines.flow.collectLatest
import messaging_api.impl.RedisImpl
import java.time.LocalDateTime

suspend fun main() {
    RedisImpl.messagesStateFlow.collectLatest {
        println("List = ${it}")
    }
    RedisImpl.sendMessage(Message("test", LocalDateTime.now(), Author("", "")))
}
