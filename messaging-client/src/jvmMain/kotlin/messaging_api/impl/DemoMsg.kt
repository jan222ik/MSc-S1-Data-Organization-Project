package messaging_api.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import messaging_api.Author
import messaging_api.IMessagingAPI
import messaging_api.Message
import java.time.LocalDateTime

val history = listOf(
    Message("Hello, my name is Holger", LocalDateTime.now(), Author("Holger", "holger@gmail.com")),
    Message("I'm Hans", LocalDateTime.now(), Author("Hans", "hans@entertainment.de")),
    Message("Nice to meet you all, call me Hugo", LocalDateTime.now(), Author("Hugo", "hugo@boss.is")),
    Message(
        "Does everyone have the agenda for today?",
        LocalDateTime.now(),
        Author("Holger", "holger@gmail.com")
    ),
    Message("If not please message me", LocalDateTime.now(), Author("Holger", "holger@gmail.com")),
    Message(
        "Hey.\nAlfons here. I would need a copy.",
        LocalDateTime.now(),
        Author("Alfons", "alfons@freemail.com")
    )
)

object DemoMsg : IMessagingAPI {
    private val internalMessagesStateFlow = MutableStateFlow(history)

    override val messagesStateFlow: StateFlow<List<Message>>
        get() = internalMessagesStateFlow

    override suspend fun sendMessage(msg: Message) {
        val l = listOf(*internalMessagesStateFlow.value.toTypedArray(), msg)
        internalMessagesStateFlow.emit(l)
    }

    override fun close() {

    }
}
