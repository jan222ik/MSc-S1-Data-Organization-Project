package messaging_api.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import messaging_api.IMessagingAPI
import messaging_api.Message
import messaging_api.RedisHandler

object RedisImpl : IMessagingAPI {

    private val handler = RedisHandler

    private val internalMessageStateFlow = MutableStateFlow<List<Message>>(listOf())

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessageStateFlow

    init {
        handler.connect(
            onNewMessage = {
                val l = listOf(*internalMessageStateFlow.value.toTypedArray(), it)
                internalMessageStateFlow.emit(l)
            }
        )
    }

    override suspend fun sendMessage(msg: Message) {
        handler.writeMessage(msg)
    }

}
