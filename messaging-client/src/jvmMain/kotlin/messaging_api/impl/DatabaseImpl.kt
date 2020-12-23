package messaging_api.impl

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import messaging_api.IMessagingAPI
import messaging_api.LettuceHandler
import messaging_api.Message
import java.io.Closeable


object DatabaseImpl : IMessagingAPI, Closeable {

    private val redisHandler = LettuceHandler()

    private val internalMessageStateFlow = MutableStateFlow<List<Message>>(listOf())

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessageStateFlow

    init {
        redisHandler.connect(
            onNextMessage = {
                val gson = Gson()
                val msg = gson.fromJson(it, Message::class.java)
                val l = listOf(*internalMessageStateFlow.value.toTypedArray(), msg)
                internalMessageStateFlow.emit(l)
            }
        )
    }

    override suspend fun sendMessage(msg: Message) {
        redisHandler.sendMessage(msg)
    }

    override fun close() {
        redisHandler.close()
    }


}
