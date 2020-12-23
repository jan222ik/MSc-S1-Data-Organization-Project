package messaging_api.impl

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import messaging_api.Author
import messaging_api.IMessagingAPI
import messaging_api.LettuceHandler
import messaging_api.Message
import java.io.Closeable
import java.time.LocalDateTime


object LettuceImpl : IMessagingAPI, Closeable {

    private val handler = LettuceHandler()

    private val internalMessageStateFlow = MutableStateFlow<List<Message>>(listOf())

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessageStateFlow

    init {
        handler.connect(
            onNextMessage = {
                val gson = Gson()
                val msg = gson.fromJson(it, Message::class.java)
                val l = listOf(*internalMessageStateFlow.value.toTypedArray(), msg)
                internalMessageStateFlow.emit(l)
            }
        )
    }

    override suspend fun sendMessage(msg: Message) {
        handler.sendMessage(msg)
    }

    override fun close() {
        handler.close()
    }


}
